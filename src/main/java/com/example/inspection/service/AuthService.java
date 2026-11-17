package com.example.inspection.service;

import com.example.inspection.dto.AuthDtos.*;
import com.example.inspection.entity.AppUser;
import com.example.inspection.exception.ApiException;
import com.example.inspection.repository.UserRepository;
import com.example.inspection.security.AppUserDetails;
import com.example.inspection.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "email already registered");
        }
        AppUser user = new AppUser();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setDisplayName(req.displayName() == null ? req.email() : req.displayName());
        userRepository.save(user);
        return issueTokens(user);
    }

    public TokenResponse login(LoginRequest req) {
        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "email or password incorrect"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "email or password incorrect");
        }
        if (user.getStatus() != 1) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_DISABLED", "user disabled");
        }
        return issueTokens(user);
    }

    public TokenResponse refresh(String rawRefresh) {
        var rotation = refreshTokenService.rotate(rawRefresh);
        AppUser user = userRepository.findById(rotation.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "user not found"));
        String access = jwtService.generateAccessToken(new AppUserDetails(user));
        return new TokenResponse(access, rotation.newToken(), jwtService.getAccessExpMs() / 1000);
    }

    private TokenResponse issueTokens(AppUser user) {
        String access = jwtService.generateAccessToken(new AppUserDetails(user));
        String refresh = refreshTokenService.issue(user.getId());
        return new TokenResponse(access, refresh, jwtService.getAccessExpMs() / 1000);
    }
}