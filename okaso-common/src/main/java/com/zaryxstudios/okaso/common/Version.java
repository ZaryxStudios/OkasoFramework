package com.zaryxstudios.okaso.common;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN =
        Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:[-+.](.+))?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final String suffix;
    private final String raw;

    public Version(int major, int minor, int patch, String suffix) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.suffix = suffix != null ? suffix : "";
        this.raw = major + "." + minor + "." + patch
            + (suffix != null && !suffix.isEmpty() ? "-" + suffix : "");
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public static Version parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        Matcher m = VERSION_PATTERN.matcher(raw.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse version: " + raw);
        }
        int major = Integer.parseInt(m.group(1));
        int minor = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        int patch = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        String suffix = m.group(4);
        return new Version(major, minor, patch, suffix);
    }

    public static Version tryParse(String raw) {
        try { return parse(raw); } catch (IllegalArgumentException e) { return null; }
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }
    public String getSuffix() { return suffix; }
    public String getRaw() { return raw; }

    public boolean hasSuffix() { return !suffix.isEmpty(); }
    public boolean isStable() { return !hasSuffix(); }

    @Override
    public int compareTo(Version other) {
        int d = Integer.compare(this.major, other.major);
        if (d != 0) return d;
        d = Integer.compare(this.minor, other.minor);
        if (d != 0) return d;
        d = Integer.compare(this.patch, other.patch);
        if (d != 0) return d;
        if (this.hasSuffix() != other.hasSuffix()) {
            return this.hasSuffix() ? -1 : 1;
        }
        return this.suffix.compareTo(other.suffix);
    }

    public boolean isNewerThan(Version other) { return compareTo(other) > 0; }
    public boolean isOlderThan(Version other) { return compareTo(other) < 0; }

    public boolean satisfies(Version minInclusive, Version maxExclusive) {
        if (minInclusive != null && compareTo(minInclusive) < 0) return false;
        if (maxExclusive != null && compareTo(maxExclusive) >= 0) return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version v = (Version) o;
        return major == v.major && minor == v.minor && patch == v.patch && suffix.equals(v.suffix);
    }

    @Override
    public int hashCode() { return Objects.hash(major, minor, patch, suffix); }

    @Override
    public String toString() { return raw; }
}
