package net.m127.vpm.repo;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.jwt.TokenRequest;
import net.m127.vpm.repo.jwt.TokenResponse;
import net.m127.vpm.repo.jwt.UserCreateRequest;
import net.m127.vpm.repo.service.EmailTakenException;
import net.m127.vpm.repo.service.NoSuchUserException;
import net.m127.vpm.repo.service.UserService;
import net.m127.vpm.repo.service.UsernameTakenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService users;
    
    public static final String LOGIN_COOKIE = "login";
    
    @Value("${jwt.secure}")
    protected boolean secure;
    
    private Cookie createTokenCookie(TokenResponse token) {
        Cookie cookie = new Cookie(LOGIN_COOKIE, token.token());
        cookie.setSecure(secure);
        cookie.setMaxAge((int) ((token.expires()-System.currentTimeMillis()) / 1000));
        return cookie;
    }
    
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> createUser(
        @RequestBody UserCreateRequest request,
        HttpServletResponse response
    ) {
        try {
            TokenResponse token = users.createUser(null, request.username(), request.email(), request.password());
            response.addCookie(createTokenCookie(token));
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(token);
        } catch (UsernameTakenException | EmailTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(
        @CookieValue(name = LOGIN_COOKIE, required = false) String creatorToken,
        @RequestBody UserCreateRequest request
    ) {
        try {
            users.createUser(creatorToken, request.username(), request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsernameTakenException | EmailTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(
        @RequestBody TokenRequest request,
        HttpServletResponse response
    ) {
        try {
            TokenResponse token = users.loginUser(request.username(), request.password());
            response.addCookie(createTokenCookie(token));
            return ResponseEntity.ok(token);
        } catch (NoSuchUserException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/approve/{username}")
    public ResponseEntity<?> approveUser(
        @CookieValue(name = LOGIN_COOKIE, required = false) String adminToken,
        @PathVariable String username
    ) {
        try {
            if(users.approveUser(adminToken, username)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (NoSuchUserException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
