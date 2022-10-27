package net.m127.vpm.repo.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "name", nullable = false, updatable = false)
    private String name;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "author", nullable = false)
    private User author;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(mappedBy = "pkg",targetEntity = PackageVersion.class)
    @OrderBy("version_major DESC, version_minor DESC, version_revision DESC")
    private List<PackageVersion> versions;
}
