# Shared helpers for run-paper.sh and run-proxy.sh. Source it; do not run it.
#
# Expects PYTHON and PAPER_CACHE to be set by the caller.

USER_AGENT="OkasoFramework-compat (https://github.com/ZaryxStudios/OkasoFramework)"
FILL_API="https://fill.papermc.io/v3/projects"

# Prints the built jar of a module, skipping what the shade plugin leaves next to it
find_jar() {
    ls "$1"/target/"$1"-*.jar 2>/dev/null | grep -vE '/original-|-shaded\.jar$|-sources\.jar$|-javadoc\.jar$' | head -n 1
}

# fill_download <project> <version> <build|latest>
# Downloads a PaperMC project build into PAPER_CACHE (checksum verified); prints the jar path
fill_download() {
    local project="$1" version="$2" build="$3" json name url sha256 jar
    echo "Resolving $project $version build $build" >&2
    json="$(curl -fsSL -A "$USER_AGENT" "$FILL_API/$project/versions/$version/builds/$build")"
    read -r name url sha256 < <(printf '%s' "$json" | "$PYTHON" -c '
import json, sys
d = json.load(sys.stdin)["downloads"]["server:default"]
print(d["name"], d["url"], d["checksums"]["sha256"])
' | tr -d '\r')
    mkdir -p "$PAPER_CACHE"
    jar="$PAPER_CACHE/$name"
    if [ ! -f "$jar" ]; then
        echo "Downloading $name" >&2
        curl -fsSL -A "$USER_AGENT" -o "$jar.part" "$url"
        mv "$jar.part" "$jar"
    fi
    if ! echo "$sha256  $jar" | sha256sum -c --quiet - >&2; then
        echo "Checksum mismatch for $jar" >&2
        rm -f "$jar"
        return 2
    fi
    echo "$jar"
}

# run_server <work-dir> <timeout> <java args...>
# Runs the server in work-dir with stdin closed, logging to work-dir/server.log; returns its exit code
run_server() {
    local dir="$1" timeout="$2"
    shift 2
    (cd "$dir" && timeout "$timeout" "$JAVA" "$@" < /dev/null > server.log 2>&1)
}

# evaluate <label> <work-dir> <result.json> <server exit code> <timeout>
# Prints GitHub annotations and returns non-zero when the smoke run failed. Checks named in
# ALLOW_FAIL are reported as warnings.
evaluate() {
    local label="$1" dir="$2" result="$3" exit_code="$4" timeout="$5" status=0 linkage
    local log="$dir/server.log"

    if [ "$exit_code" -eq 124 ]; then
        echo "::error::$label did not stop within ${timeout}s"
        status=1
    fi
    if [ ! -f "$result" ]; then
        echo "::error::$label: $result was not written (Okaso or the smoke plugin did not run)"
        status=1
    else
        "$PYTHON" - "$result" "$label" "${ALLOW_FAIL:-}" <<'EOF' || status=1
import json, sys
result, label, allowed = json.load(open(sys.argv[1], encoding="utf-8")), sys.argv[2], sys.argv[3]
allowed = {name.strip() for name in allowed.split(",") if name.strip()}
failed = {check["name"]: check["error"] for check in result["checks"] if check["error"]}
for name in sorted(allowed - set(failed)):
    print(f"::notice::{label}: known failure '{name}' now passes; remove it from ALLOW_FAIL")
for name in sorted(set(failed) & allowed):
    print(f"::warning::{label}: known failure {name}: {failed[name]}")
unexpected = sorted(set(failed) - allowed)
for name in unexpected:
    print(f"::error::{label}: {name}: {failed[name]}")
sys.exit(1 if unexpected else 0)
EOF
    fi
    # Linkage errors can surface outside the smoke checks, e.g. while Okaso enables
    linkage="$(grep -nE 'NoSuchMethodError|NoSuchFieldError|NoClassDefFoundError|ExceptionInInitializerError|AbstractMethodError|IncompatibleClassChangeError' "$log" | grep -i 'okaso' || true)"
    if [ -n "$linkage" ]; then
        echo "::error::$label: linkage errors mentioning Okaso"
        echo "$linkage"
        status=1
    fi

    grep -E 'OKASO-SMOKE' "$log" || true
    [ -f "$result" ] && cat "$result"
    if [ "$status" -ne 0 ]; then
        echo "---- last 80 lines of $log ----"
        tail -n 80 "$log"
    fi
    return "$status"
}
