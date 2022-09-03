package net.m127.vpm.repo.service;

import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.PackageVersion;
import net.m127.vpm.repo.jpa.entity.SemVersion;
import net.m127.vpm.repo.json.PackageAuthor;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.PackageListing;
import net.m127.vpm.repo.json.RepoListing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RepoListingFactoryImpl implements RepoListingFactory {
    @Value("${vpm.repository.name}")
    protected String name;
    @Value("${vpm.repository.author}")
    protected String author;
    
    @Override
    public RepoListing createListing(String url, Iterable<Package> packages) {
        final String urlPart = url.replaceFirst("index\\.json$", "packages");
        return new RepoListing(
            name,
            author,
            url,
            StreamSupport.stream(packages.spliterator(), false)
                         .collect(Collectors.toUnmodifiableMap(
                             Package::getId,
                             pkg -> createPackageListing(pkg, urlPart)
                         ))
        );
    }
    
    private PackageListing createPackageListing(Package pkg, final String urlPart) {
        Map<SemVersion, PackageJson> versionMap = pkg
            .getVersions()
            .entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> {
                    PackageVersion version = e.getValue();
                    return new PackageJson(version, urlPart);
                }
            ));
        return new PackageListing(
            pkg.getId(),
            pkg.getDisplayName(),
            pkg.getDescription(),
            new PackageAuthor(pkg.getAuthor()),
            versionMap
        );
    }
}
