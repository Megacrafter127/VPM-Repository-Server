package net.m127.vpm.repo.json;

import net.m127.vpm.repo.jpa.entity.SemVersion;

import java.util.Map;

public record PackageListing(
        String id,
        String displayName,
        String description,
        PackageAuthor author,
        Map<SemVersion, PackageJson> versions
) {
}
