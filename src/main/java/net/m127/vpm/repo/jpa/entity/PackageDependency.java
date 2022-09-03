package net.m127.vpm.repo.jpa.entity;

import javax.persistence.*;

@Entity
@Table(name="dependencies")
public class PackageDependency {
    @EmbeddedId
    private PackageDependencyRef id;
    @Column(name = "dependency_version")
    private String version;
    
    public PackageDependency(PackageDependencyRef id, String version) {
        this.id = id;
        this.version = version;
    }
    
    public PackageDependency() {}
    
    public PackageDependencyRef getId() {
        return id;
    }
    
    public void setId(PackageDependencyRef id) {
        this.id = id;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
