package net.m127.vpm.repo;

import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.PackageMetaData;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.service.FileService;
import net.m127.vpm.repo.service.VPMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;
import java.util.zip.ZipException;

@RestController
@RequestMapping("/vpm")
public class RestAPI {
    @Autowired
    protected VPMService vpmService;
    @Autowired
    protected FileService fileService;
    
    private final Logger logger = LoggerFactory.getLogger(RestAPI.class);
    
    @GetMapping("/index.json")
    public RepoListing getIndex(HttpServletRequest request) {
        return vpmService.getRepoListing(request.getRequestURL().toString());
    }
    
    @PutMapping("/packages/{packageId}")
    public ResponseEntity<?> createPackage(
        HttpServletRequest request,
        @PathVariable String packageId,
        @RequestBody PackageMetaData pkg
    ) throws IOException {
        return switch (vpmService.createPackage(
            packageId,
            request.getUserPrincipal().getName(),
            pkg.displayName(),
            pkg.description()
        )) {
            case OK -> ResponseEntity.created(URI.create(request.getRequestURI())).build();
            case EXISTENCE -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            case NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
    
    @PostMapping("/packages")
    public ResponseEntity<?> uploadVersion(HttpServletRequest request, @RequestParam MultipartFile files) {
        try{
            PackageJson result = vpmService.uploadPackage(files);
            return ResponseEntity.created(
                URI.create(String.format(
                    "%s/%s/%s.zip",
                    request.getRequestURI(),
                    result.name(),
                    result.version()
                ))
            ).build();
        } catch(AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch(FileNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch(FileAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch(ZipException ex) {
            return ResponseEntity.unprocessableEntity().build();
        } catch(IOException ex) {
            logger.error("Error writing package zip file", ex);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/packages/{packageId}")
    public PackageMetaData getPackage(@PathVariable String packageId) {
        return new PackageMetaData(vpmService.getPackage(packageId));
    }
    
    @DeleteMapping("/packages/{packageId}")
    public ResponseEntity<?> deletePackage(
        HttpServletRequest request,
        @PathVariable String packageId
    ) throws IOException {
        return switch (vpmService.deletePackage(packageId, request.getUserPrincipal().getName())) {
            case OK -> ResponseEntity.accepted().build();
            case EXISTENCE -> ResponseEntity.notFound().build();
            case NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
    
    @GetMapping("/packages/{packageId}/{packageVersion}.zip")
    public ResponseEntity<Resource> getZip(@PathVariable String packageId, @PathVariable String packageVersion) {
        Resource file = null;
        try{
            file = fileService.getPackageVersion(packageId, packageVersion);
        } catch(IOException ex) {
            logger.error("Error reading package zip file", ex);
        }
        return ResponseEntity.of(
            Optional
                .ofNullable(file)
                .filter(Resource::exists)
        );
    }
}
