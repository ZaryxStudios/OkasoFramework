"""Prints the GitHub Actions matrix for the Paper smoke tests.

Each entry has the Minecraft version, the Java version Paper requires for it (read from the
Paper API) and the smoke checks that are known to fail there. The newest stable Paper release
is added automatically, so new Minecraft versions are tested without editing the workflow.

Usage: python3 scripts/compat/paper-matrix.py
"""
import json
import re
import urllib.request

API = "https://fill.papermc.io/v3/projects/paper"
USER_AGENT = "OkasoFramework-compat (https://github.com/ZaryxStudios/OkasoFramework)"

# Oldest supported version first; keep one entry per API era worth covering
VERSIONS = [
    "1.8.8", "1.12.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4",
    "1.20.4", "1.20.6", "1.21.4", "1.21.11",
]

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


def latest_stable():
    for releases in get(API)["versions"].values():
        for version in releases:
            if re.fullmatch(r"\d+(\.\d+)+", version):
                return version
    raise SystemExit("No stable Paper version found")


def allowed_failures(version):
    names = []
    for check, since, until, _reason in KNOWN_FAILURES:
        if key(version) >= key(since) and (until is None or key(version) < key(until)):
            names.append(check)
    return ",".join(names)


def main():
    versions = list(VERSIONS)
    newest = latest_stable()
    if newest not in versions:
        versions.append(newest)
    include = []
    for version in versions:
        java = get(f"{API}/versions/{version}")["version"]["java"]["version"]["minimum"]
        include.append({"mc": version, "java": str(java), "allow_fail": allowed_failures(version)})
    print(json.dumps({"include": include}))


if __name__ == "__main__":
    main()
