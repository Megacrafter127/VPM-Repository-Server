package net.m127.vpm.repo.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.m127.vpm.repo.json.SemVersion;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "package_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "package", updatable = false, nullable = false)
    private Package pkg;
    
    @Column(name = "version_major", nullable = false, updatable = false)
    private int major;
    @Column(name = "version_minor", nullable = false, updatable = false)
    private int minor;
    @Column(name = "version_revision", nullable = false, updatable = false)
    private int revision;
    
    @OneToMany(mappedBy = "dependent", cascade = CascadeType.ALL)
    @MapKey(name = "dependency")
    private Map<String, PackageDependency> dependencies;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "blob", nullable = false)
    private PackageBlob blob;
    
    public PackageVersion(Package pkg, SemVersion version, byte[] zipFile) {
        this(null, pkg, version.major(), version.minor(), version.revision(), null, null);
        this.blob = new PackageBlob(null, this, zipFile);
    }
}
