package com.example.inspection.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessExpMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpMs = accessExpMs;
    }

    public String generateAccessToken(AppUserDetails user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessExpMs() {
        return accessExpMs;
    }
}