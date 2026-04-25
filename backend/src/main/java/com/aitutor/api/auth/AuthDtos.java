package com.aitutor.api.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RegisterRequest(
        @NotBlank String username,
        @Size(min = 6, max = 128) String password,
        @NotBlank String displayName,
        @Min(3) @Max(18) int age,
        @Min(1) @Max(12) int gradeLevel,
        @NotBlank String language
) {
}

record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}

record AuthResponse(String token, StudentResponse student) {
}

record StudentResponse(
        String id,
        String username,
        String displayName,
        int age,
        int gradeLevel,
        String language
) {
}
