package net.m127.vpm.repo.json;

import net.m127.vpm.repo.jpa.entity.PackageDependency;
import net.m127.vpm.repo.jpa.entity.PackageVersion;
import net.m127.vpm.repo.jpa.entity.SemVersion;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public record PackageJson(
    String name,
    SemVersion version,
    String displayName,
    String description,
    PackageAuthor author,
    String url,
    Map<String, String> dependencies,
    Map<String, String> vpmDependencies
) {
    public PackageJson(PackageVersion version, String urlPart) {
        this(
            version.getId().getPkg().getId(),
            version.getId().getVersion(),
            version.getId().getPkg().getDisplayName(),
            version.getId().getPkg().getDescription(),
            new PackageAuthor(version.getId().getPkg().getAuthor()),
            String.format("%s/%s/%s.zip",
                          urlPart,
                          version.getId().getPkg().getId(),
                          version.getId().getVersion().toString()
            ),
            Collections.emptyMap(),
            version
                .getDependencies()
                .values()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                    dep -> dep.getId().getDependency(),
                    PackageDependency::getVersion
                ))
        );
    }
}
