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

source "$(dirname "$0")/lib.sh"

OKASO_JAR="${OKASO_JAR:-$(find_jar okaso-bukkit)}"
SMOKE_JAR="${SMOKE_JAR:-$(find_jar okaso-smoke)}"
[ -f "$OKASO_JAR" ] || { echo "Okaso jar not found; run mvn install first" >&2; exit 2; }
[ -f "$SMOKE_JAR" ] || { echo "Smoke plugin jar not found; run mvn install first" >&2; exit 2; }

SERVER_JAR="$(fill_download paper "$MC_VERSION" "$BUILD")"

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

echo "Starting Paper $MC_VERSION ($(basename "$SERVER_JAR")) with $("$JAVA" -version 2>&1 | head -n 1)"
set +e
run_server "$WORK_DIR" "$TIMEOUT" -Xms512M -Xmx1G \
    -Dcom.mojang.eula.agree=true -DIReallyKnowWhatIAmDoingISwear=true -Dpaper.disableChannelLimit=true \
    -jar paper.jar nogui
EXIT_CODE=$?
set -e

evaluate "Paper $MC_VERSION" "$WORK_DIR" "$WORK_DIR/plugins/OkasoSmoke/result.json" "$EXIT_CODE" "$TIMEOUT"
