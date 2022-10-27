package net.m127.vpm.repo.jpa.entity;

import java.io.Serializable;

public record PackageDependencyRef(PackageVersion dependent, String dependency) implements Serializable {
}
