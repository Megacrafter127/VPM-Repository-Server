package net.m127.vpm.repo.json;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.m127.vpm.repo.jpa.entity.Package;

import java.util.List;

public record PackageMetaDataVersion(
    @JsonUnwrapped PackageMetaData packageMetaData,
    List<SemVersion> versions
) {
    public PackageMetaDataVersion(Package pkg) {
        this(
            new PackageMetaData(pkg),
            pkg.getVersions()
                .stream()
                .map(v -> new SemVersion(
                    v.getMajor(),
                    v.getMinor(),
                    v.getRevision()
                ))
                .toList()
        );
    }
}
