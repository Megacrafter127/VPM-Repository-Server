package net.m127.vpm.repo.json;

import net.m127.vpm.repo.jpa.entity.Package;

public record PackageMetaData(
    String id,
    String displayName,
    String description,
    PackageAuthor author
) {
    public PackageMetaData(Package pkg) {
        this(pkg.getName(), pkg.getDisplayName(), pkg.getDescription(), new PackageAuthor(pkg.getAuthor()));
    }
}
