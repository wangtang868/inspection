package com.example.inspection.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    public record RegisterRequest(
            @Email @NotBlank
            String email,
            @NotBlank @Size(min = 8, max = 64)
            String password,
            String displayName) {

    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}
}