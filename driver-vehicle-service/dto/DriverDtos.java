package lk.ridelink.driver_vehicle_service.dto;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ridelink.driver_vehicle_service.model.DriverAvailability;
import lk.ridelink.driver_vehicle_service.model.VehicleType;

public final class DriverDtos {

    private DriverDtos() {
    }

    public record CoordinateRequest(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
    }

    public record ServiceAreaRequest(
            @NotNull @Valid CoordinateRequest center,
            @NotNull @DecimalMin("1.0") @DecimalMax("200.0") Double radiusKm) {
    }

    public record OperationalProfileRequest(
            @NotBlank @Size(max = 50) String driverLicenseNumber,
            @NotNull @FutureOrPresent LocalDate licenseExpiryDate,
            @NotNull @Valid ServiceAreaRequest serviceArea) {
    }

    public record VehicleRequest(
            @NotBlank @Size(max = 20) String registrationNumber,
            @NotNull VehicleType type,
            @NotBlank @Size(max = 50) String make,
            @NotBlank @Size(max = 50) String model,
            @Min(1990) @Max(2100) int year,
            @NotBlank @Size(max = 30) String color,
            @Min(1) @Max(20) int seatingCapacity) {
    }

    public record LocationRequest(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
    }

    public record AvailabilityRequest(@NotNull DriverAvailability availability) {
    }

    public record CoordinateResponse(double latitude, double longitude) {
    }

    public record ServiceAreaResponse(CoordinateResponse center, double radiusKm) {
    }

    public record VehicleResponse(
            String registrationNumber,
            VehicleType type,
            String make,
            String model,
            int year,
            String color,
            int seatingCapacity) {
    }

    public record DriverProfileResponse(
            String id,
            String accountId,
            String driverLicenseNumber,
            LocalDate licenseExpiryDate,
            DriverAvailability availability,
            ServiceAreaResponse serviceArea,
            CoordinateResponse currentLocation,
            VehicleResponse vehicle,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record EligibleDriverResponse(
            String driverAccountId,
            VehicleResponse vehicle,
            CoordinateResponse currentLocation,
            double distanceKm) {
    }
}