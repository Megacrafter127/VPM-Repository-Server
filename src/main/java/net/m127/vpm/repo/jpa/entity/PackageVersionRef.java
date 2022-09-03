package net.m127.vpm.repo.jpa.entity;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Comparator;

@Embeddable
public class PackageVersionRef implements Comparable<PackageVersionRef>, Serializable {
    public static final Comparator<PackageVersionRef> ORDER = Comparator
        .comparing((PackageVersionRef ref) -> ref.getPkg().getId())
        .thenComparing(PackageVersionRef::getVersion);
    @ManyToOne(optional = false)
    @JoinColumn(name = "package", referencedColumnName = "id", updatable = false, nullable = false)
    private transient Package pkg;
    @Embedded
    private SemVersion version;
    
    public PackageVersionRef(Package pkg, SemVersion version) {
        this.pkg = pkg;
        this.version = version;
    }
    
    public PackageVersionRef() {}
    
    public Package getPkg() {
        return pkg;
    }
    
    public void setPkg(Package pkg) {
        this.pkg = pkg;
    }
    
    public SemVersion getVersion() {
        return version;
    }
    
    public void setVersion(SemVersion version) {
        this.version = version;
    }
    
    @Override
    public int compareTo(PackageVersionRef other) {
        return ORDER.compare(this, other);
    }
}
