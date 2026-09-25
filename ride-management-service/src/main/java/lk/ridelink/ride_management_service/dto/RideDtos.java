package lk.ridelink.ride_management_service.dto;

import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ridelink.ride_management_service.model.RideStatus;

public final class RideDtos {

    private RideDtos() {
    }

    public record PlaceRequest(
            @NotBlank @Size(max = 250) String address,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
    }

    public record CreateRideRequest(
            @NotNull @Valid PlaceRequest pickup,
            @NotNull @Valid PlaceRequest destination) {
    }

    public record CancelRideRequest(@Size(max = 500) String reason) {
    }

    public record PlaceResponse(String address, double latitude, double longitude) {
    }

    public record RideResponse(
            String id,
            String passengerAccountId,
            String assignedDriverAccountId,
            PlaceResponse pickup,
            PlaceResponse destination,
            RideStatus status,
            Double assignmentDistanceKm,
            String cancellationReason,
            Instant createdAt,
            Instant updatedAt,
            Instant assignedAt,
            Instant acceptedAt,
            Instant startedAt,
            Instant completedAt,
            Instant cancelledAt) {
    }
}
