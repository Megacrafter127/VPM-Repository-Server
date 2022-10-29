package net.m127.vpm.repo;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.json.PackageJson;
import net.m127.vpm.repo.json.PackageMetaData;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.service.NoSuchUserException;
import net.m127.vpm.repo.service.VPMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/vpm")
@RequiredArgsConstructor
public class VpmRepositoryController {
    private final VPMService vpmService;
    
    private final Logger log = LoggerFactory.getLogger(VpmRepositoryController.class);
    
    @GetMapping("/index.json")
    public RepoListing getIndex(HttpServletRequest request) {
        return vpmService.getRepoListing(request.getRequestURL().toString());
    }
    
    @PutMapping("/packages/{packageId}")
    public ResponseEntity<?> createPackage(
        HttpServletRequest request,
        @CookieValue(name = UserController.LOGIN_COOKIE, required = false) String token,
        @PathVariable String packageId,
        @RequestBody PackageMetaData pkg
    ) {
        if(token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    public ResponseEntity<?> uploadVersion(
        HttpServletRequest request,
        @CookieValue(name = UserController.LOGIN_COOKIE, required = false) String token,
        @RequestBody Resource file
    ) {
        if(token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            PackageJson result = vpmService.uploadPackage(token, request.getRequestURL().toString(), file.getInputStream());
            return ResponseEntity.created(URI.create(result.url())).body(result);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchUserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IOException e) {
            log.error("IO Error while processing package", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/packages/{packageId}")
    public PackageMetaData getPackage(@PathVariable String packageId) {
        return new PackageMetaData(vpmService.getPackage(packageId));
    }
    
    @DeleteMapping("/packages/{packageId}/{major}.{minor}.{revision}.zip")
    public ResponseEntity<?> deletePackageVersion(
        @CookieValue(name = UserController.LOGIN_COOKIE, required = false) String token,
        @PathVariable String packageId,
        @PathVariable int major,
        @PathVariable int minor,
        @PathVariable int revision
    ) {
        if(token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return switch (vpmService.deletePackageVersion(packageId, major, minor, revision, token)) {
            case OK -> ResponseEntity.accepted().build();
            case EXISTENCE -> ResponseEntity.notFound().build();
            case NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
    
    @DeleteMapping("/packages/{packageId}")
    public ResponseEntity<?> deletePackage(
        @CookieValue(name = UserController.LOGIN_COOKIE, required = false) String token,
        @PathVariable String packageId
    ) {
        if(token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return switch (vpmService.deletePackage(packageId, token)) {
            case OK -> ResponseEntity.accepted().build();
            case EXISTENCE -> ResponseEntity.notFound().build();
            case NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
    
    @GetMapping(value = "/packages/{packageId}/{major}.{minor}.{revision}.zip", produces = {"application/zip"})
    public ResponseEntity<? extends Resource> getZip(
        @PathVariable final String packageId,
        @PathVariable final int major,
        @PathVariable final int minor,
        @PathVariable final int revision
    ) {
        final ContentDisposition disposition = ContentDisposition
            .attachment()
            .filename(
                String.format(
                    "%s_%d.%d.%d.zip",
                    packageId,
                    major,
                    minor,
                    revision
                )
            ).build();
        return Optional
                .ofNullable(vpmService.getPackageZip(packageId, major, minor, revision))
                .map(ByteArrayResource::new)
                .filter(Resource::exists)
            .map(r -> ResponseEntity
                .ok()
                .headers(h -> h.setContentDisposition(disposition))
                .body(r)
            ).orElseGet(() -> ResponseEntity
                .notFound()
                .headers(h -> h.setContentDisposition(disposition))
                .build()
            );
    }
}
