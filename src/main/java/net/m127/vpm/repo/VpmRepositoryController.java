package net.m127.vpm.repo;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.PackageMetaData;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.service.VPMService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/vpm")
@RequiredArgsConstructor
public class VpmRepositoryController {
    private final VPMService vpmService;
    
    @GetMapping("/index.json")
    public RepoListing getIndex(HttpServletRequest request) {
        return vpmService.getRepoListing(request.getRequestURL().toString());
    }
    
    @PutMapping("/packages/{packageId}")
    public ResponseEntity<?> createPackage(
        HttpServletRequest request,
        @PathVariable String packageId,
        @RequestBody PackageMetaData pkg
    ) {
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
    public ResponseEntity<?> uploadVersion(HttpServletRequest request, @RequestBody byte[] file) {
        try{
            PackageJson result = vpmService.uploadPackage(file);
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
    ) {
        return switch (vpmService.deletePackage(packageId, request.getUserPrincipal().getName())) {
            case OK -> ResponseEntity.accepted().build();
            case EXISTENCE -> ResponseEntity.notFound().build();
            case NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
    
    @GetMapping("/packages/{packageId}/{major}.{minor}.{revision}.zip")
    public ResponseEntity<? extends Resource> getZip(
        @PathVariable String packageId,
        @PathVariable int major,
        @PathVariable int minor,
        @PathVariable int revision
    ) {
        return ResponseEntity.of(
            Optional
                .ofNullable(vpmService.getPackageZip(packageId, major, minor, revision))
                .map(ByteArrayResource::new)
                .filter(Resource::exists)
        );
    }
}