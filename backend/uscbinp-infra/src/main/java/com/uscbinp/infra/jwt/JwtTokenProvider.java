package com.uscbinp.infra.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final long EXPIRE_MS = 3_600_000;

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${uscbinp.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String subject) {
        Date now = new Date();
        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + EXPIRE_MS))
            .signWith(secretKey)
            .compact();
    }

    public String parseSubject(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}
