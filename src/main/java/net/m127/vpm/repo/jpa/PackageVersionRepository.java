package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.PackageVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageVersionRepository extends JpaRepository<PackageVersion, Long> {
    PackageVersion findByPkgAndMajorAndMinorAndRevision(Package pkg, int major, int minor, int revision);
    
    boolean existsByPkgAndMajorAndMinorAndRevision(Package pkg, int major, int minor, int revision);
}
