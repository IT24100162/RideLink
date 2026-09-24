package lk.ridelink.account_service.dto;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ridelink.account_service.model.AccountRole;
import lk.ridelink.account_service.model.AccountStatus;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 10, max = 100) String password,
            @NotBlank @Size(max = 30) String phone,
            @NotNull AccountRole role) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            AccountResponse account) {
    }

    public record AccountResponse(
            String id,
            String fullName,
            String email,
            String phone,
            AccountRole role,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ProfileUpdateRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Size(max = 30) String phone) {
    }

    public record RoleUpdateRequest(@NotNull AccountRole role) {
    }

    public record StatusUpdateRequest(@NotNull AccountStatus status) {
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}