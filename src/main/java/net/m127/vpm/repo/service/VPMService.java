package net.m127.vpm.repo.service;

import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.User;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface VPMService {
    User getCurrentUser();
    
    User getUser(String username);
    
    List<User> getUsers();
    
    Package getPackage(String packageId);
    
    User getPackageAuthor(String packageId);
    
    List<Package> getUserPackages(String username);
    
    List<Package> getAllPackages();
    
    RepoActionResult createPackage(String packageId, String author, String displayName, String description);
    
    RepoActionResult deletePackage(String packageId, String author);
    
    PackageJson uploadPackage(byte[] file) throws AccessDeniedException;
    
    byte[] getPackageZip(String pkg, int major, int minor, int revision);
    
    RepoListing getRepoListing(String url);
}
