package net.m127.vpm.repo.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "package_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageDependency implements Serializable {
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "dependent",
        nullable = false,
        updatable = false
    )
    private PackageVersion dependent;
    
    @Id
    @Column(name = "dependency", nullable = false, updatable = false)
    private String dependency;
    
    @Column(name = "dependency_version", nullable = false)
    private String version;
}
