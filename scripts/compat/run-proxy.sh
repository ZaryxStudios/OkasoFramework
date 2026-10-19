#!/usr/bin/env bash
# Boots a proxy with the matching Okaso adapter and the okaso-smoke plugin, then checks the result.
#
# Usage: scripts/compat/run-proxy.sh <bungeecord|waterfall|velocity> [version]
#
#   bungeecord  latest successful build from the SpigotMC Jenkins (no version argument)
#   waterfall   PaperMC build of the given version (default: 1.21, the last release)
#   velocity    PaperMC build of the given version (default: newest stable)
#
# Environment: JAVA, PYTHON, PAPER_CACHE, TIMEOUT, ALLOW_FAIL as in run-paper.sh, plus
#   OKASO_JAR    adapter jar (default: okaso-<platform>/target/okaso-<platform>-<version>.jar)
#   SMOKE_JAR    smoke plugin jar (default: okaso-smoke/target/okaso-smoke-<version>.jar)
#   WORK_DIR     proxy directory, wiped on start (default: target/compat/<platform>-<version>)
set -euo pipefail

PLATFORM="${1:?usage: run-proxy.sh <bungeecord|waterfall|velocity> [version]}"
VERSION="${2:-}"
JAVA="${JAVA:-java}"
PYTHON="${PYTHON:-python3}"
PAPER_CACHE="${PAPER_CACHE:-.compat-cache}"
TIMEOUT="${TIMEOUT:-180}"

source "$(dirname "$0")/lib.sh"

case "$PLATFORM" in
    bungeecord)
        JENKINS="https://hub.spigotmc.org/jenkins/job/BungeeCord"
        BUILD_NUMBER="$(curl -fsSL -A "$USER_AGENT" "$JENKINS/lastSuccessfulBuild/api/json?tree=number" \
            | "$PYTHON" -c 'import json, sys; print(json.load(sys.stdin)["number"])' | tr -d '\r')"
        VERSION="build-$BUILD_NUMBER"
        PROXY_JAR="$PAPER_CACHE/BungeeCord-$BUILD_NUMBER.jar"
        mkdir -p "$PAPER_CACHE"
        if [ ! -f "$PROXY_JAR" ]; then
            echo "Downloading BungeeCord build $BUILD_NUMBER"
            # The SpigotMC Jenkins publishes no checksum for this artifact
            curl -fsSL -A "$USER_AGENT" -o "$PROXY_JAR.part" "$JENKINS/$BUILD_NUMBER/artifact/bootstrap/target/BungeeCord.jar"
            mv "$PROXY_JAR.part" "$PROXY_JAR"
        fi
        RESULT_DIR="OkasoSmoke"
        ;;
    waterfall)
        VERSION="${VERSION:-1.21}"
        PROXY_JAR="$(fill_download waterfall "$VERSION" latest)"
        RESULT_DIR="OkasoSmoke"
        ;;
    velocity)
        if [ -z "$VERSION" ]; then
            VERSION="$(curl -fsSL -A "$USER_AGENT" "$FILL_API/velocity" | "$PYTHON" -c '
import json, re, sys
for releases in json.load(sys.stdin)["versions"].values():
    stable = [v for v in releases if re.fullmatch(r"\d+(\.\d+)+", v)]
    if stable:
        print(stable[0])
        break
' | tr -d '\r')"
        fi
        PROXY_JAR="$(fill_download velocity "$VERSION" latest)"
        RESULT_DIR="okaso-smoke"
        ;;
    *)
        echo "Unknown platform: $PLATFORM" >&2
        exit 2
        ;;
esac

OKASO_JAR="${OKASO_JAR:-$(find_jar "okaso-$PLATFORM")}"
SMOKE_JAR="${SMOKE_JAR:-$(find_jar okaso-smoke)}"
[ -f "$OKASO_JAR" ] || { echo "okaso-$PLATFORM jar not found; run mvn install first" >&2; exit 2; }
[ -f "$SMOKE_JAR" ] || { echo "Smoke plugin jar not found; run mvn install first" >&2; exit 2; }

WORK_DIR="${WORK_DIR:-target/compat/$PLATFORM-$VERSION}"
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/plugins"
cp "$PROXY_JAR" "$WORK_DIR/proxy.jar"
cp "$OKASO_JAR" "$WORK_DIR/plugins/okaso-$PLATFORM.jar"
cp "$SMOKE_JAR" "$WORK_DIR/plugins/okaso-smoke.jar"

echo "Starting $PLATFORM $VERSION with $("$JAVA" -version 2>&1 | head -n 1)"
set +e
run_server "$WORK_DIR" "$TIMEOUT" -Xms256M -Xmx512M -jar proxy.jar
EXIT_CODE=$?
set -e

evaluate "$PLATFORM $VERSION" "$WORK_DIR" "$WORK_DIR/plugins/$RESULT_DIR/result.json" "$EXIT_CODE" "$TIMEOUT"
