package net.m127.vpm.repo.jpa.entity;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "package_versions")
public class PackageVersion {
    @EmbeddedId
    private PackageVersionRef id;
    
    @OneToMany(mappedBy = "id.dependent")
    @MapKeyColumn(name = "dependency")
    private Map<String, PackageDependency> dependencies;
    
    public PackageVersion(PackageVersionRef id) {
        this.id = id;
    }
    
    public PackageVersion() {}
    
    public PackageVersionRef getId() {
        return id;
    }
    
    public void setId(PackageVersionRef id) {
        this.id = id;
    }
    
    public Map<String, PackageDependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(Map<String, PackageDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
