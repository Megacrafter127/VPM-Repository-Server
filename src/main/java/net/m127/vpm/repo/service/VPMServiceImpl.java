package net.m127.vpm.repo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.PackageRepository;
import net.m127.vpm.repo.jpa.PackageVersionRepository;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.*;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.permission.UserPermission;
import net.m127.vpm.repo.util.ZipUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VPMServiceImpl implements VPMService {
    private final UserService users;
    private final RepoListingFactory repoListingFactory;
    private final PackageRepository packages;
    private final PackageVersionRepository packageVersions;
    private final ZipUtil zipUtil;
    @Value("${permission.package.creation}")
    protected UserPermission packageCreation;
    @Value("${permission.package.upload}")
    protected UserPermission packageUpload;
    
    private static Optional<User> matchesPermission(Optional<User> user, UserPermission permission) {
        return switch (permission) {
            case ADMIN -> user.filter(User::isAdmin);
            case APPROVED -> user.filter(User::isApproved);
            case VALIDATED -> user.filter(User::isValidated);
            default -> user;
        };
    }
    
    @Override
    public Optional<Package> getPackage(String packageId) {
        return packages.findByName(packageId);
    }
    
    @Override
    public RepoActionResult createPackage(
        String token,
        String packageId,
        String author,
        String displayName,
        String description
    ) {
        if (packages.existsByName(packageId)) return RepoActionResult.EXISTENCE;
        return matchesPermission(
            users.getCurrentUser(token),
            packageCreation
        ).flatMap(
            user -> {
                if(author == null) return Optional.of(user);
                if(user.getName().equals(author)) return Optional.of(user);
                if(user.isAdmin()) {
                    return users.getUser(author);
                } else {
                    return Optional.empty();
                }
            }
        ).map(user -> {
            packages.save(new Package(null, packageId, user, displayName, description, null));
            return RepoActionResult.OK;
        }).orElse(RepoActionResult.NOT_ALLOWED);
    }
    
    @Override
    public RepoActionResult deletePackage(final String packageId, final String token) {
        Optional<User> pkg = packages.findByName(packageId)
            .map(Package::getAuthor);
        if (pkg.isEmpty()) return RepoActionResult.EXISTENCE;
        return pkg.filter(user -> user.equals(users.getCurrentUser(token).orElse(null)))
            .map(ignoredAuthor -> {
                packages.deleteByName(packageId);
                return RepoActionResult.OK;
            })
            .orElse(RepoActionResult.NOT_ALLOWED);
    }
    
    public List<Package> getAllPackages() {
        return packages.findAll();
    }
    
    @Override
    public RepoActionResult deletePackageVersion(String packageId, int major, int minor, int revision, String token) {
        if (!packages.existsByName(packageId)) return RepoActionResult.EXISTENCE;
        Optional<Package> opkg = packages.findByName(packageId);
        if (opkg.isEmpty()) return RepoActionResult.EXISTENCE;
        Package pkg = opkg.get();
        if (users.getCurrentUser(token).filter(pkg.getAuthor()::equals).isEmpty()) return RepoActionResult.NOT_ALLOWED;
        return packageVersions.findByPkgAndMajorAndMinorAndRevision(pkg, major, minor, revision)
            .map(version -> {
                packageVersions.delete(version);
                return RepoActionResult.OK;
            })
            .orElse(RepoActionResult.EXISTENCE);
    }
    
    @Override
    public PackageJson uploadPackage(String token, final String url, InputStream file)
        throws AccessDeniedException, NoSuchUserException, IOException {
        Optional<User> permittedUser = users.getCurrentUser(token);
        final User currentUser = permittedUser.orElseThrow(NoSuchUserException::new);
        permittedUser = matchesPermission(permittedUser, packageUpload);
        final User permitted = permittedUser.orElseThrow(() -> new AccessDeniedException(
            String.format("'%s' lacks the required permissions to upload", currentUser.getName()))
        );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PackageJson json = zipUtil.alterPackageJSONInFlight(
            file,
            buffer,
            j -> j.withURL(repoListingFactory.toPackageURL(url, j.name(), j.version()))
        );
        Package pkg = packages.findByName(json.name())
            .orElseGet(() -> {
                Optional<User> creationUser = matchesPermission(
                    Optional.of(permitted),
                    packageCreation
                );
                Package npkg = new Package(
                    null,
                    json.name(),
                    creationUser.orElseThrow(() -> new AccessDeniedException(
                        String.format("'%s' lacks the required permissions to upload", currentUser.getName()))
                    ),
                    json.displayName(),
                    json.description(),
                    null
                );
                packages.save(npkg);
                return npkg;
            });
        if (!pkg.getAuthor().equals(currentUser)) {
            throw new AccessDeniedException("Package not owned by User");
        }
        if (packageVersions.existsByPkgAndMajorAndMinorAndRevision(
            pkg,
            json.version().major(),
            json.version().minor(),
            json.version().revision()
        )) {
            throw new IllegalStateException("Specified version already exists");
        }
        PackageVersion version = new PackageVersion(pkg, json.version(), buffer.toByteArray());
        if (json.vpmDependencies() != null) {
            version.setDependencies(
                json.vpmDependencies()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new PackageDependency(version, e.getKey(), e.getValue())
                    ))
            );
        }
        packageVersions.saveAndFlush(version);
        return json;
    }
    
    @Override
    public Optional<List<Package>> getCurrentUserPackages(String token) {
        return users.getCurrentUser(token)
            .map(User::getUserPackages);
    }
    
    public byte[] getPackageZip(String pkg, int major, int minor, int revision) {
        log.debug("Trying to load version {}.{}.{} of package {}", major, minor, revision, pkg);
        return packages.findByName(pkg)
            .flatMap(pack -> packageVersions.findByPkgAndMajorAndMinorAndRevision(
                pack, major, minor, revision
            ))
            .map(PackageVersion::getBlob)
            .map(PackageBlob::getZipFile)
            .orElse(null);
    }
    
    @Override
    public RepoListing getRepoListing(String url) {
        return repoListingFactory.createListing(url, getAllPackages());
    }
}
