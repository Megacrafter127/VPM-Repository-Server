package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackageRepository extends JpaRepository<Package, Long> {
    Optional<Package> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
