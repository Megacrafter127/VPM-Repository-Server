package net.m127.vpm.repo.jpa.entity;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class PackageDependencyRef implements Serializable {
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "dependent",
        referencedColumnName = "package",
        nullable = false,
        updatable = false
    )
    @JoinColumn(
        name = "version_major",
        referencedColumnName = "version_major",
        nullable = false,
        updatable = false
    )
    @JoinColumn(
        name = "version_minor",
        referencedColumnName = "version_minor",
        nullable = false,
        updatable = false
    )
    @JoinColumn(
        name = "version_revision",
        referencedColumnName = "version_revision",
        nullable = false,
        updatable = false
    )
    private transient PackageVersion dependent;
    
    @Column(name = "dependency", nullable = false, updatable = false)
    private String dependency;
    
    public PackageDependencyRef(PackageVersion dependent, String dependency) {
        this.dependent = dependent;
        this.dependency = dependency;
    }
    
    public PackageDependencyRef() {}
    
    public PackageVersion getDependent() {
        return dependent;
    }
    
    public void setDependent(PackageVersion dependent) {
        this.dependent = dependent;
    }
    
    public String getDependency() {
        return dependency;
    }
    
    public void setDependency(String dependency) {
        this.dependency = dependency;
    }
}
