package net.m127.vpm.repo.jpa.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public class SemVersion implements Comparable<SemVersion>, Serializable {
    public static final Comparator<SemVersion> ORDER = Comparator
        .comparingInt(SemVersion::getMajor)
        .thenComparingInt(SemVersion::getMinor)
        .thenComparingInt(SemVersion::getRevision);
    
    public static final Pattern PATTERN = Pattern.compile("(?<major>\\d++)\\.(?<minor>\\d++)\\.(?<revision>\\d++)");
    
    @Column(name = "version_major")
    private int major;
    @Column(name = "version_minor")
    private int minor;
    @Column(name = "version_revision")
    private int revision;
    
    public SemVersion(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }
    
    public SemVersion() {}
    
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
    
    public int getMajor() {
        return major;
    }
    
    public void setMajor(int major) {
        this.major = major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public void setMinor(int minor) {
        this.minor = minor;
    }
    
    public int getRevision() {
        return revision;
    }
    
    public void setRevision(int revision) {
        this.revision = revision;
    }
    
    @Override
    public int compareTo(SemVersion semVersion) {
        return ORDER.compare(this, semVersion);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemVersion that)) return false;
        return major == that.major && minor == that.minor && revision == that.revision;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, revision);
    }
    
    @JsonValue
    @JsonKey
    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, revision);
    }
}
