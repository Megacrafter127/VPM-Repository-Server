package net.m127.vpm.repo.service;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.PackageRepository;
import net.m127.vpm.repo.jpa.PackageVersionRepository;
import net.m127.vpm.repo.jpa.UserRepository;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.PackageDependency;
import net.m127.vpm.repo.jpa.entity.PackageVersion;
import net.m127.vpm.repo.jpa.entity.User;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.jwt.TokenManager;
import net.m127.vpm.repo.permission.AutoPackageCreation;
import net.m127.vpm.repo.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VPMServiceImpl implements VPMService {
    private final UserRepository users;
    private final TokenManager tokenManager;
    private final RepoListingFactory repoListingFactory;
    private final PackageRepository packages;
    private final PackageVersionRepository packageVersions;
    private final ZipUtil zipUtil;
    private final Logger log = LoggerFactory.getLogger(VPMServiceImpl.class);
    @Value("${permission.package.creation.automatic}")
    protected AutoPackageCreation automaticPackageCreation;
    
    @Override
    public User getCurrentUser(String token) {
        if (token == null) {
            log.trace("Null token");
            return null;
        }
        String creator = tokenManager.getUsernameFromValidToken(token);
        if (creator == null) {
            log.trace("Invalid token");
            return null;
        }
        if (!users.existsByName(creator)) {
            log.trace("Nonexistant User token");
            return null;
        }
        return users.findByName(creator);
    }
    
    @Override
    public User getUser(String username) {
        return users.findByName(username);
    }
    
    @Override
    public List<User> getUsers() {
        return users.findAll();
    }
    
    @Override
    public Package getPackage(String packageId) {
        if (!packages.existsByName(packageId)) return null;
        return packages.findByName(packageId);
    }
    
    @Override
    public User getPackageAuthor(String packageId) {
        return getPackage(packageId).getAuthor();
    }
    
    @Override
    public RepoActionResult createPackage(String packageId, String token, String displayName, String description) {
        if (packages.existsByName(packageId)) return RepoActionResult.EXISTENCE;
        packages.save(new Package(null, packageId, getCurrentUser(token), displayName, description, null));
        return RepoActionResult.OK;
    }
    
    @Override
    public RepoActionResult deletePackage(String packageId, String token) {
        if (!packages.existsByName(packageId)) return RepoActionResult.EXISTENCE;
        if (!packages.findByName(packageId).getAuthor().equals(getCurrentUser(token))) {
            return RepoActionResult.NOT_ALLOWED;
        }
        packages.deleteByName(packageId);
        return RepoActionResult.OK;
    }
    
    @Override
    public List<Package> getUserPackages(String username) {
        return users.findByName(username).getUserPackages();
    }
    
    public List<Package> getAllPackages() {
        return packages.findAll();
    }
    
    @Override
    public RepoActionResult deletePackageVersion(String packageId, int major, int minor, int revision, String token) {
        if(!packages.existsByName(packageId)) return RepoActionResult.EXISTENCE;
        Package pkg = packages.findByName(packageId);
        if(!pkg.getAuthor().equals(getCurrentUser(token))) return RepoActionResult.NOT_ALLOWED;
        if(!packageVersions.existsByPkgAndMajorAndMinorAndRevision(pkg, major, minor, revision)) return RepoActionResult.EXISTENCE;
        PackageVersion version = packageVersions.findByPkgAndMajorAndMinorAndRevision(pkg, major, minor, revision);
        packageVersions.delete(version);
        return RepoActionResult.OK;
    }
    
    @Override
    public PackageJson uploadPackage(String token, final String url, InputStream file)
        throws AccessDeniedException, NoSuchUserException, IOException {
        final User currentUser = getCurrentUser(token);
        if (currentUser == null) throw new NoSuchUserException();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PackageJson json = zipUtil.alterPackageJSONInFlight(
            file,
            buffer,
            j -> j.withURL(repoListingFactory.toPackageURL(url, j.name(), j.version()))
        );
        Package pkg;
        if (!packages.existsByName(json.name())) {
            if (automaticPackageCreation != AutoPackageCreation.ANY) {
                throw new NoSuchElementException(
                    "Package not registered");
            }
            pkg = new Package(
                null,
                json.name(),
                getCurrentUser(token),
                json.displayName(),
                json.description(),
                null
            );
            packages.save(pkg);
        } else {
            pkg = packages.findByName(json.name());
        }
        if (!pkg.getAuthor().equals(getCurrentUser(token))) {
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
    
    public byte[] getPackageZip(String pkg, int major, int minor, int revision) {
        log.debug("Trying to load version {}.{}.{} of package {}", major, minor, revision, pkg);
        if(!packages.existsByName(pkg)) return null;
        Package pack = packages.findByName(pkg);
        if(!packageVersions.existsByPkgAndMajorAndMinorAndRevision(
            pack, major, minor, revision
        )) return null;
        return packageVersions
                .findByPkgAndMajorAndMinorAndRevision(
                    pack, major, minor, revision
                )
                .getBlob()
                .getZipFile();
    }
    
    @Override
    public RepoListing getRepoListing(String url) {
        return repoListingFactory.createListing(url, getAllPackages());
    }
}
