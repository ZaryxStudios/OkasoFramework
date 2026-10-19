#!/usr/bin/env bash
# Boots a Paper server with Okaso and the okaso-smoke plugin, then checks the result.
#
# Usage: scripts/compat/run-paper.sh <minecraft-version> [build|latest]
#
# Running this accepts the Minecraft EULA (https://aka.ms/MinecraftEULA) for the test server.
#
# Environment:
#   JAVA         java executable to run the server with (default: java)
#   PYTHON       python executable used to read the Paper API (default: python3)
#   OKASO_JAR    Okaso Bukkit jar (default: okaso-bukkit/target/okaso-bukkit-<version>.jar)
#   SMOKE_JAR    smoke plugin jar (default: okaso-smoke/target/okaso-smoke-<version>.jar)
#   PAPER_CACHE  where downloaded server jars are kept (default: .compat-cache)
#   WORK_DIR     server directory, wiped on start (default: target/compat/paper-<mc-version>)
#   TIMEOUT      seconds before the server is killed (default: 300)
#   ALLOW_FAIL   comma-separated smoke checks that are known to fail on this version; they are
#                reported as warnings instead of failing the run
set -euo pipefail

MC_VERSION="${1:?usage: run-paper.sh <minecraft-version> [build|latest]}"
BUILD="${2:-latest}"
JAVA="${JAVA:-java}"
PYTHON="${PYTHON:-python3}"
PAPER_CACHE="${PAPER_CACHE:-.compat-cache}"
WORK_DIR="${WORK_DIR:-target/compat/paper-$MC_VERSION}"
TIMEOUT="${TIMEOUT:-300}"
ALLOW_FAIL="${ALLOW_FAIL:-}"
API="https://fill.papermc.io/v3/projects/paper"
USER_AGENT="OkasoFramework-compat (https://github.com/ZaryxStudios/OkasoFramework)"

find_jar() {
    # The shade plugin leaves original-* and *-shaded.jar next to the real jar
    ls "$1"/target/"$1"-*.jar 2>/dev/null | grep -vE '/original-|-shaded\.jar$|-sources\.jar$|-javadoc\.jar$' | head -n 1
}

OKASO_JAR="${OKASO_JAR:-$(find_jar okaso-bukkit)}"
SMOKE_JAR="${SMOKE_JAR:-$(find_jar okaso-smoke)}"
[ -f "$OKASO_JAR" ] || { echo "Okaso jar not found; run mvn install first" >&2; exit 2; }
[ -f "$SMOKE_JAR" ] || { echo "Smoke plugin jar not found; run mvn install first" >&2; exit 2; }

echo "Resolving Paper $MC_VERSION build $BUILD"
BUILD_JSON="$(curl -fsSL -A "$USER_AGENT" "$API/versions/$MC_VERSION/builds/$BUILD")"
read -r JAR_NAME JAR_URL JAR_SHA256 < <(printf '%s' "$BUILD_JSON" | "$PYTHON" -c '
import json, sys
d = json.load(sys.stdin)["downloads"]["server:default"]
print(d["name"], d["url"], d["checksums"]["sha256"])
' | tr -d '\r')

mkdir -p "$PAPER_CACHE"
SERVER_JAR="$PAPER_CACHE/$JAR_NAME"
if [ ! -f "$SERVER_JAR" ]; then
    echo "Downloading $JAR_NAME"
    curl -fsSL -A "$USER_AGENT" -o "$SERVER_JAR.part" "$JAR_URL"
    mv "$SERVER_JAR.part" "$SERVER_JAR"
fi
echo "$JAR_SHA256  $SERVER_JAR" | sha256sum -c --quiet - || { echo "Checksum mismatch for $SERVER_JAR" >&2; rm -f "$SERVER_JAR"; exit 2; }

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/plugins"
cp "$SERVER_JAR" "$WORK_DIR/paper.jar"
cp "$OKASO_JAR" "$WORK_DIR/plugins/okaso-bukkit.jar"
cp "$SMOKE_JAR" "$WORK_DIR/plugins/okaso-smoke.jar"

echo "eula=true" > "$WORK_DIR/eula.txt"
cat > "$WORK_DIR/server.properties" <<'EOF'
online-mode=false
level-type=FLAT
generate-structures=false
allow-nether=false
spawn-protection=0
spawn-monsters=false
spawn-animals=false
view-distance=2
simulation-distance=2
server-ip=127.0.0.1
server-port=25565
enable-query=false
enable-rcon=false
EOF
cat > "$WORK_DIR/bukkit.yml" <<'EOF'
settings:
  allow-end: false
EOF

echo "Starting Paper $MC_VERSION ($JAR_NAME) with $("$JAVA" -version 2>&1 | head -n 1)"
set +e
(
    cd "$WORK_DIR" &&
    timeout "$TIMEOUT" "$JAVA" -Xms512M -Xmx1G \
        -Dcom.mojang.eula.agree=true -DIReallyKnowWhatIAmDoingISwear=true -Dpaper.disableChannelLimit=true \
        -jar paper.jar nogui < /dev/null > server.log 2>&1
)
EXIT_CODE=$?
set -e

LOG="$WORK_DIR/server.log"
RESULT="$WORK_DIR/plugins/OkasoSmoke/result.json"
STATUS=0

if [ "$EXIT_CODE" -eq 124 ]; then
    echo "::error::Paper $MC_VERSION did not stop within ${TIMEOUT}s"
    STATUS=1
fi
if [ ! -f "$RESULT" ]; then
    echo "::error::Paper $MC_VERSION: $RESULT was not written (Okaso or the smoke plugin did not run)"
    STATUS=1
else
    "$PYTHON" - "$RESULT" "$MC_VERSION" "$ALLOW_FAIL" <<'EOF' || STATUS=1
import json, sys
result, version, allowed = json.load(open(sys.argv[1], encoding="utf-8")), sys.argv[2], sys.argv[3]
allowed = {name.strip() for name in allowed.split(",") if name.strip()}
failed = {check["name"]: check["error"] for check in result["checks"] if check["error"]}
for name in sorted(allowed - set(failed)):
    print(f"::notice::Paper {version}: known failure '{name}' now passes; remove it from ALLOW_FAIL")
for name in sorted(set(failed) & allowed):
    print(f"::warning::Paper {version}: known failure {name}: {failed[name]}")
unexpected = sorted(set(failed) - allowed)
for name in unexpected:
    print(f"::error::Paper {version}: {name}: {failed[name]}")
sys.exit(1 if unexpected else 0)
EOF
fi
# Linkage errors can surface outside the smoke checks, e.g. while Okaso enables
LINKAGE="$(grep -nE 'NoSuchMethodError|NoSuchFieldError|NoClassDefFoundError|ExceptionInInitializerError|AbstractMethodError|IncompatibleClassChangeError' "$LOG" | grep -i 'okaso' || true)"
if [ -n "$LINKAGE" ]; then
    echo "::error::Paper $MC_VERSION: linkage errors mentioning Okaso"
    echo "$LINKAGE"
    STATUS=1
fi

grep -E 'OKASO-SMOKE' "$LOG" || true
[ -f "$RESULT" ] && cat "$RESULT"
if [ "$STATUS" -ne 0 ]; then
    echo "---- last 80 lines of $LOG ----"
    tail -n 80 "$LOG"
fi
exit "$STATUS"
