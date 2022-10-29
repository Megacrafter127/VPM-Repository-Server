package net.m127.vpm.repo.json;

import net.m127.vpm.repo.jpa.entity.PackageDependency;
import net.m127.vpm.repo.jpa.entity.PackageVersion;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
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
            version.getPkg().getName(),
            new SemVersion(
                version.getMajor(),
                version.getMinor(),
                version.getRevision()
            ),
            version.getPkg().getDisplayName(),
            version.getPkg().getDescription(),
            new PackageAuthor(version.getPkg().getAuthor()),
            String.format("%s/%s/%d.%d.%d.zip",
                          urlPart,
                          version.getPkg().getName(),
                          version.getMajor(),
                          version.getMinor(),
                          version.getRevision()
            ),
            Collections.emptyMap(),
            version
                .getDependencies()
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    ((Function<PackageDependency, String>)PackageDependency::getVersion)
                        .compose(Map.Entry::getValue)
                ))
        );
    }
    
    public PackageJson withURL(String url) {
        return new PackageJson(name, version, displayName, description, author, url, dependencies, vpmDependencies);
    }
}
