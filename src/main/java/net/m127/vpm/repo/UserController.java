package net.m127.vpm.repo;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.jwt.TokenRequest;
import net.m127.vpm.repo.jwt.TokenResponse;
import net.m127.vpm.repo.jwt.UserCreateRequest;
import net.m127.vpm.repo.service.EmailTakenException;
import net.m127.vpm.repo.service.NoSuchUserException;
import net.m127.vpm.repo.service.UserService;
import net.m127.vpm.repo.service.UsernameTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService users;
    
    public static final String LOGIN_COOKIE = "login";
    
    private static Cookie createTokenCookie(TokenResponse token) {
        Cookie cookie = new Cookie(LOGIN_COOKIE, token.token());
        cookie.setSecure(true);
        cookie.setMaxAge((int) ((token.expires()-System.currentTimeMillis()) / 1000));
        return cookie;
    }
    
    @PostMapping("")
    public ResponseEntity<TokenResponse> createUser(
        @CookieValue(name = LOGIN_COOKIE, required = false) String creatorToken,
        @RequestBody UserCreateRequest request,
        HttpServletResponse response
    ) {
        try {
            TokenResponse token = users.createUser(creatorToken, request.username(), request.email(), request.password());
            response.addCookie(createTokenCookie(token));
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (UsernameTakenException | EmailTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @GetMapping("")
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
}