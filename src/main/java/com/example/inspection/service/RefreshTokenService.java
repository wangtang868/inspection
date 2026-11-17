package com.example.inspection.service;


import com.example.inspection.entity.RefreshToken;
import com.example.inspection.exception.ApiException;
import com.example.inspection.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    public String issue(Long userId) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setTokenHash(sha256(raw));
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpMs));
        repo.save(rt);
        return raw;
    }

    @Transactional
    public RotationResult rotate(String raw) {
        String hash = sha256(raw);
        RefreshToken current = repo.findByTokenHash(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_INVALID", "refresh token invalid"));

        if (current.isRevoked()) {
            repo.revokeAllByUserId(current.getUserId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_REUSED", "refresh token reused");
        }
        if (current.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED", "refresh token expired");
        }
        current.setRevoked(true);
        repo.save(current);
        String newRaw = issue(current.getUserId());
        return new RotationResult(current.getUserId(), newRaw);
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record RotationResult(Long userId, String newToken) {}
}