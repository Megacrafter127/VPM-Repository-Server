package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.PackageVersion;
import net.m127.vpm.repo.jpa.entity.PackageVersionRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageVersionRepository extends JpaRepository<PackageVersion, PackageVersionRef> {
}
