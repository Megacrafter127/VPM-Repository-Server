package net.m127.vpm.repo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenManager {
    private final String jwtSecret;
    private final long tokenLifetime;
    
    private final JwtParser parser;
    
    public TokenManager(
        @Value("${jwt.tokenLifetime}") long lifetime,
        @Value("${jwt.secret}") String base64
    ) {
        jwtSecret = base64;
        tokenLifetime = lifetime;
        parser = Jwts.parserBuilder().setSigningKey(jwtSecret).build();
    }
    
    public TokenResponse generateJwtToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return new TokenResponse(
            Jwts.builder().setClaims(claims).setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenLifetime * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact(),
            tokenLifetime
        );
    }
    
    public boolean validateJwtToken(String token, String username) {
        Claims claims = parser.parseClaimsJws(token).getBody();
        boolean isTokenExpired = claims.getExpiration().before(new Date());
        return (claims.getSubject().equals(username)) && !isTokenExpired;
    }
    
    public String getUsernameFromValidToken(String token) {
        final Claims claims = parser.parseClaimsJws(token).getBody();
        if(claims.getExpiration().before(new Date())) return null;
        return claims.getSubject();
    }
}
