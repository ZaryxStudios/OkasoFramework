"""Prints the GitHub Actions matrix for the smoke tests.

Paper entries have the Minecraft version, the Java version Paper requires for it (read from the
PaperMC API) and the smoke checks that are known to fail there. The newest stable Paper and
Velocity releases are added automatically, so new versions are tested without editing the
workflow.

Usage: python3 scripts/compat/matrix.py [--proxies]
"""
import json
import re
import sys
import urllib.request

FILL = "https://fill.papermc.io/v3/projects"
USER_AGENT = "OkasoFramework-compat (https://github.com/ZaryxStudios/OkasoFramework)"

# Oldest supported version first; keep one entry per API era worth covering
VERSIONS = [
    "1.8.8", "1.12.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4",
    "1.20.4", "1.20.6", "1.21.4", "1.21.11",
]

# Last Velocity 3.x release; the newest stable release is added on top
VELOCITY_VERSIONS = ["3.5.1"]
# The SpigotMC Jenkins does not publish a Java requirement for BungeeCord, and the PaperMC API
# reports Java 8 for Waterfall 1.21 although its bootstrap needs Java 11; both are pinned to
# versions verified to start
BUNGEECORD_JAVA = "21"
WATERFALL_JAVA = "17"

# (check, first version where it fails, first version where it works again or None, reason)
KNOWN_FAILURES = [
    ("nbt", "1.17", None, "okaso-nbt looks up pre-1.17 NMS names; 1.20.5+ items use data components"),
]


def get(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def key(version):
    return tuple(int(part) for part in version.split("."))


def latest_stable(project="paper"):
    for releases in get(f"{FILL}/{project}")["versions"].values():
        for version in releases:
            if re.fullmatch(r"\d+(\.\d+)+", version):
                return version
    raise SystemExit(f"No stable {project} version found")


def java_for(project, version):
    return str(get(f"{FILL}/{project}/versions/{version}")["version"]["java"]["version"]["minimum"])


def allowed_failures(version):
    names = []
    for check, since, until, _reason in KNOWN_FAILURES:
        if key(version) >= key(since) and (until is None or key(version) < key(until)):
            names.append(check)
    return ",".join(names)


def paper_matrix():
    versions = list(VERSIONS)
    newest = latest_stable()
    if newest not in versions:
        versions.append(newest)
    return [{"mc": v, "java": java_for("paper", v), "allow_fail": allowed_failures(v)} for v in versions]


def proxy_matrix():
    include = [{"platform": "bungeecord", "version": "", "java": BUNGEECORD_JAVA},
               {"platform": "waterfall", "version": "1.21", "java": WATERFALL_JAVA}]
    velocity = list(VELOCITY_VERSIONS)
    newest = latest_stable("velocity")
    if newest not in velocity:
        velocity.append(newest)
    include += [{"platform": "velocity", "version": v, "java": java_for("velocity", v)} for v in velocity]
    return include


def main():
    include = proxy_matrix() if "--proxies" in sys.argv[1:] else paper_matrix()
    print(json.dumps({"include": include}))


if __name__ == "__main__":
    main()
