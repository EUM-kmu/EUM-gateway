package com.eum.gateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.Key;
@Slf4j
@Service
public class JwtService {
    private static final String USER_ID = "userId";
    private static final String UID = "uid";
    private static final String ROLE = "role";
    private static final String BEARER_TYPE = "Bearer";
    private final Key key;

    public JwtService() {
        byte[] keyBytes = Decoders.BASE64.decode("VlwEyVBsYt9V7zq57TejMnVUyzblYcfPQye08f7MGVA9XkHN");
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JwtService 클래스
    public Claims isJwtValid(String jwt) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
            // 401 Unauthorized
            throw new RuntimeException("Invalid JWT Token: " + e.getMessage(), e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            // 401 Expired
            throw new RuntimeException("Expired JWT Token: " + e.getMessage(), e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            // 400 Bad Request
            throw new RuntimeException("Unsupported JWT Token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
            // 400 Bad Request
            throw new RuntimeException("JWT claims string is empty: " + e.getMessage(), e);
        }
    }

}
