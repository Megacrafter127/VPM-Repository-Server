package net.m127.vpm.repo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TokenManager {
    private final SecretKey jwtSecret;
    private final long tokenLifetime;
    private final JwtParser parser;
    
    public TokenManager(
        @Value("${jwt.tokenLifetime}") long lifetime
    ) throws NoSuchAlgorithmException {
        tokenLifetime = lifetime;
        jwtSecret = KeyGenerator.getInstance("HmacSHA512")
            .generateKey();
        parser = Jwts.parserBuilder().setSigningKey(jwtSecret).build();
    }
    
    public TokenResponse generateJwtToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return new TokenResponse(
            Jwts.builder().setClaims(claims).setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenLifetime * 1000))
                .signWith(jwtSecret).compact(),
            tokenLifetime
        );
    }
    
    public String getUsernameFromValidToken(String token) {
        try {
            final Claims claims = parser.parseClaimsJws(token).getBody();
            if(claims.getExpiration().before(new Date())) {
                log.trace("Token Expired");
                return null;
            }
            log.trace("Valid token for {}", claims.getSubject());
            return claims.getSubject();
        } catch(SignatureException ex) {
            log.trace("Rejected token", ex);
            return null;
        }
    }
}
