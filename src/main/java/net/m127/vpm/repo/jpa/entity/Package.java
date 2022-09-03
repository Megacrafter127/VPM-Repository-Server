package net.m127.vpm.repo.jpa.entity;

import javax.persistence.*;
import java.util.SortedMap;

@Entity
@Table(name = "packages")
public class Package {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "author", nullable = false)
    private User author;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(mappedBy = "id.pkg")
    @OrderBy
    private SortedMap<SemVersion, PackageVersion> versions;
    
    public Package() {}
    
    public Package(String id, User author, String displayName, String description) {
        this.id = id;
        this.author = author;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public SortedMap<SemVersion, PackageVersion> getVersions() {
        return versions;
    }
    
    public void setVersions(SortedMap<SemVersion, PackageVersion> versions) {
        this.versions = versions;
    }
}
