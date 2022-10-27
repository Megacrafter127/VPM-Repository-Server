package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<Package, Long> {
    Package findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
