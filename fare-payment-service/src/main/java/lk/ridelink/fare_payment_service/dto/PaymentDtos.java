package lk.ridelink.fare_payment_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ridelink.fare_payment_service.model.PaymentStatus;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record CoordinatePair(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {}

    public record FareEstimateRequest(
            @NotNull @Valid CoordinatePair pickup,
            @NotNull @Valid CoordinatePair destination) {}

    public record FareEstimateResponse(
            String currency,
            BigDecimal straightLineDistanceKm,
            BigDecimal billableDistanceKm,
            BigDecimal baseFare,
            BigDecimal distanceFare,
            BigDecimal serviceFee,
            BigDecimal totalFare,
            String pricingRule,
            Instant calculatedAt) {}

    public record ChargeRequest(
            @NotBlank @Size(max = 100) String rideId,
            @NotNull Boolean simulateFailure) {}

    public record PaymentResponse(
            String id,
            String rideId,
            String passengerAccountId,
            String driverAccountId,
            String currency,
            BigDecimal straightLineDistanceKm,
            BigDecimal billableDistanceKm,
            BigDecimal baseFare,
            BigDecimal distanceFare,
            BigDecimal serviceFee,
            BigDecimal totalFare,
            PaymentStatus status,
            String failureMessage,
            String receiptNumber,
            Instant createdAt) {}

    public record ReceiptResponse(
            String receiptNumber,
            String paymentId,
            String rideId,
            String passengerAccountId,
            String currency,
            BigDecimal totalFare,
            PaymentStatus paymentStatus,
            Instant issuedAt) {}
}
