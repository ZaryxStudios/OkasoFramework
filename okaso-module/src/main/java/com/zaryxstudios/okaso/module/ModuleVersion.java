package com.zaryxstudios.okaso.module;

public class ModuleVersion implements Comparable<ModuleVersion> {

    private final int major;
    private final int minor;
    private final int patch;
    private final String preRelease;
    private final String build;

    public ModuleVersion(int major, int minor, int patch, String preRelease, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.build = build;
    }

    public static ModuleVersion parse(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        String base = version;
        String build = "";
        String preRelease = "";

        int buildIdx = version.indexOf('+');
        if (buildIdx >= 0) {
            build = version.substring(buildIdx + 1);
            base = version.substring(0, buildIdx);
        }

        int preIdx = base.indexOf('-');
        if (preIdx >= 0) {
            preRelease = base.substring(preIdx + 1);
            base = base.substring(0, preIdx);
        }

        String[] parts = base.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            return new ModuleVersion(major, minor, patch, preRelease, build);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version number in: " + version, e);
        }
    }

    @Override
    public int compareTo(ModuleVersion other) {
        if (major != other.major) return Integer.compare(major, other.major);
        if (minor != other.minor) return Integer.compare(minor, other.minor);
        if (patch != other.patch) return Integer.compare(patch, other.patch);

        boolean hasPre = !preRelease.isEmpty();
        boolean otherHasPre = !other.preRelease.isEmpty();
        if (hasPre != otherHasPre) return hasPre ? -1 : 1;
        if (hasPre) return preRelease.compareTo(other.preRelease);

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor).append('.').append(patch);
        if (!preRelease.isEmpty()) sb.append('-').append(preRelease);
        if (!build.isEmpty()) sb.append('+').append(build);
        return sb.toString();
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }
    public String getPreRelease() { return preRelease; }
    public String getBuild() { return build; }
}
