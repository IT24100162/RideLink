package lk.ridelink.account_service.dto;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
            @Past LocalDate dateOfBirth,
            @Size(max = 500) String profileImageUrl,
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
            LocalDate dateOfBirth,
            String profileImageUrl,
            AccountRole role,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ProfileUpdateRequest(
            @Size(max = 100) String fullName,
            @Size(max = 30) String phone,
            @Past LocalDate dateOfBirth,
            @Size(max = 500) String profileImageUrl) {
    }

    public record EmailChangeRequest(
            @NotBlank String currentPassword,
            @NotBlank @Email @Size(max = 254) String newEmail) {
    }

    public record PasswordChangeRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 10, max = 100) String newPassword) {
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