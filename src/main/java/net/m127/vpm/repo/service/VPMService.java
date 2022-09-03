package net.m127.vpm.repo.service;

import net.m127.vpm.repo.RepoActionResult;
import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.jpa.entity.User;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.RepoListing;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VPMService {
    User getCurrentUser();
    
    User getUser(String username);
    
    List<User> getUsers();
    
    Package getPackage(String packageId);
    
    User getPackageAuthor(String packageId);
    
    List<Package> getUserPackages(String username);
    
    List<Package> getAllPackages();
    
    RepoActionResult createPackage(String packageId, String author, String displayName, String description)
        throws IOException;
    
    RepoActionResult deletePackage(String packageId, String author) throws IOException;
    
    PackageJson uploadPackage(MultipartFile file) throws IOException,
        AccessDeniedException;
    
    RepoListing getRepoListing(String url);
}
