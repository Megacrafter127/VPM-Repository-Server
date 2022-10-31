package net.m127.vpm.repo.service;

import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface VPMService {
    
    Optional<Package> getPackage(String packageId);
    
    List<Package> getAllPackages();
    
    RepoActionResult createPackage(String token, String packageId, String author, String displayName, String description);
    
    RepoActionResult deletePackage(String packageId, String token);
    
    RepoActionResult deletePackageVersion(String packageId, int major, int minor, int revision, String token);
    
    PackageJson uploadPackage(String token, String url, InputStream file)
        throws AccessDeniedException, NoSuchUserException, IOException;
    
    byte[] getPackageZip(String pkg, int major, int minor, int revision);
    
    RepoListing getRepoListing(String url);
}
