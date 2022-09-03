package net.m127.vpm.repo.service;

import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.PackageVersionRepository;
import net.m127.vpm.repo.jpa.entity.*;
import net.m127.vpm.repo.jpa.PackageRepository;
import net.m127.vpm.repo.jpa.UserRepository;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VPMServiceImpl implements VPMService {
    @Autowired
    protected UserRepository users;
    @Autowired
    protected RepoListingFactory repoListingFactory;
    @Autowired
    protected PackageRepository packages;
    @Autowired
    protected PackageVersionRepository packageVersions;
    @Autowired
    protected FileService fileService;
    
    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            return getUser(auth.getName());
        } else {
            return null;
        }
    }
    
    @Override
    public User getUser(String username) {
        return users.getReferenceById(username);
    }
    
    @Override
    public List<User> getUsers() {
        return users.findAll();
    }
    
    @Override
    public Package getPackage(String packageId) {
        if (!packages.existsById(packageId)) return null;
        return packages.getReferenceById(packageId);
    }
    
    @Override
    public User getPackageAuthor(String packageId) {
        return getPackage(packageId).getAuthor();
    }
    
    @Override
    public RepoActionResult createPackage(String packageId, String author, String displayName, String description)
        throws IOException {
        if (packages.existsById(packageId)) return RepoActionResult.EXISTENCE;
        fileService.initializePackage(packageId);
        packages.save(new Package(packageId, getUser(author), displayName, description));
        return RepoActionResult.OK;
    }
    
    @Override
    public RepoActionResult deletePackage(String packageId, String author) throws IOException {
        if (!packages.existsById(packageId)) return RepoActionResult.EXISTENCE;
        if (!packages.getReferenceById(packageId).getAuthor().getName().equals(author)) {
            return RepoActionResult.NOT_ALLOWED;
        }
        packages.deleteById(packageId);
        fileService.deletePackage(packageId);
        return RepoActionResult.OK;
    }
    
    @Override
    public List<Package> getUserPackages(String username) {
        return users.getReferenceById(username).getUserPackages();
    }
    
    public List<Package> getAllPackages() {
        return packages.findAll();
    }
    
    @Override
    public PackageJson uploadPackage(MultipartFile file) throws IOException, AccessDeniedException {
        FileService.ParsedFileUpload parsed = fileService.parseFileUpload(file);
        if(!packages.existsById(parsed.parsedJson().name())) {
            throw new FileNotFoundException("Package not registered");
        }
        Package pkg = packages.getReferenceById(parsed.parsedJson().name());
        if (!pkg.getAuthor().equals(getCurrentUser())) {
            throw new AccessDeniedException("Package not owned by User");
        }
        PackageVersionRef ref = new PackageVersionRef(pkg, parsed.parsedJson().version());
        if (packageVersions.existsById(ref)) {
            throw new FileAlreadyExistsException("Specified version already exists");
        }
        fileService.saveFileUpload(parsed);
        PackageVersion version = new PackageVersion(ref);
        if(parsed.parsedJson().vpmDependencies() != null) {
            version.setDependencies(
                parsed
                    .parsedJson()
                    .vpmDependencies()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new PackageDependency(new PackageDependencyRef(version, e.getKey()), e.getValue())
                    ))
            );
        }
        packageVersions.saveAndFlush(version);
        return parsed.parsedJson();
    }
    
    @Override
    public RepoListing getRepoListing(String url) {
        return repoListingFactory.createListing(url, getAllPackages());
    }
}
