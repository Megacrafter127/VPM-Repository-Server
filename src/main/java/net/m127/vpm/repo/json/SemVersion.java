package net.m127.vpm.repo.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SemVersion(int major, int minor, int revision) implements Comparable<SemVersion> {
    public static final Comparator<SemVersion> ORDER = Comparator
        .comparingInt(SemVersion::major)
        .thenComparingInt(SemVersion::minor)
        .thenComparingInt(SemVersion::revision);
    
    public static final Pattern PATTERN = Pattern.compile("(?<major>\\d++)\\.(?<minor>\\d++)\\.(?<revision>\\d++)");
    
    @JsonCreator
    public static SemVersion parse(String json) {
        Matcher m = PATTERN.matcher(json);
        if (!m.matches()) throw new IllegalArgumentException("Not a SemVersion string");
        return new SemVersion(
            Integer.parseInt(m.group("major")),
            Integer.parseInt(m.group("minor")),
            Integer.parseInt(m.group("revision"))
        );
    }
    
    @Override
    public int compareTo(SemVersion semVersion) {
        return ORDER.compare(this, semVersion);
    }
    
    @JsonValue
    @JsonKey
    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, revision);
    }
}
