package net.m127.vpm.repo.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "package_blobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageBlob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @OneToOne(mappedBy = "blob", cascade = CascadeType.ALL, orphanRemoval = true)
    private PackageVersion packageVersion;
    
    @Column(name = "zipFile", nullable = false, updatable = false)
    private byte[] zipFile;
}
