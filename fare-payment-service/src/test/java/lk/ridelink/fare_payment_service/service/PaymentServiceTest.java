package lk.ridelink.fare_payment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.fare_payment_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.fare_payment_service.client.RideClient;
import lk.ridelink.fare_payment_service.client.RideClient.Place;
import lk.ridelink.fare_payment_service.client.RideClient.RideSnapshot;
import lk.ridelink.fare_payment_service.dto.PaymentDtos.ChargeRequest;
import lk.ridelink.fare_payment_service.model.Payment;
import lk.ridelink.fare_payment_service.model.PaymentStatus;
import lk.ridelink.fare_payment_service.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository repository;
    @Mock RideClient rideClient;
    private PaymentService service;
    private AccountIdentity passenger;
    private RideSnapshot completedRide;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, rideClient, new FareCalculator());
        passenger = new AccountIdentity("passenger-1", "p@example.com", "PASSENGER", "ACTIVE");
        completedRide = new RideSnapshot("ride-1", passenger.id(), "driver-1",
                new Place("Colombo Fort", 6.9344, 79.8428),
                new Place("Bambalapitiya", 6.8941, 79.8560), "COMPLETED");
    }

    @Test
    void successfulChargeStoresPaymentAndGeneratesReceipt() {
        when(rideClient.getRide("Bearer token", "ride-1")).thenReturn(completedRide);
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-1");
            return payment;
        });
        when(repository.findByRideId("ride-1")).thenReturn(Optional.empty());
        var result = service.charge(new ChargeRequest("ride-1", false), "Bearer token", passenger);
        assertEquals(PaymentStatus.SUCCEEDED, result.status());
        assertEquals("LKR", result.currency());
        assertNotNull(result.receiptNumber());
        assertNotNull(result.totalFare());
        verify(repository).save(any(Payment.class));
    }

    @Test
    void simulatedDeclineIsPersistedWithoutReceipt() {
        when(rideClient.getRide("Bearer token", "ride-1")).thenReturn(completedRide);
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-1");
            return payment;
        });
        when(repository.findByRideId("ride-1")).thenReturn(Optional.empty());
        var result = service.charge(new ChargeRequest("ride-1", true), "Bearer token", passenger);
        assertEquals(PaymentStatus.FAILED, result.status());
        assertNull(result.receiptNumber());
        assertNotNull(result.failureMessage());
    }

    @Test
    void paymentIsRejectedUntilRideIsCompleted() {
        when(rideClient.getRide("Bearer token", "ride-1"))
                .thenReturn(new RideSnapshot("ride-1", passenger.id(), "driver-1",
                        completedRide.pickup(), completedRide.destination(), "IN_PROGRESS"));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.charge(new ChargeRequest("ride-1", false), "Bearer token", passenger));
        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).save(any(Payment.class));
    }

    @Test
    void onlyRidePassengerCanPay() {
        when(rideClient.getRide("Bearer token", "ride-1")).thenReturn(completedRide);
        AccountIdentity stranger = new AccountIdentity("other-passenger", "x@example.com", "PASSENGER", "ACTIVE");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.charge(new ChargeRequest("ride-1", false), "Bearer token", stranger));
        assertEquals(403, error.getStatusCode().value());
    }

    @Test
    void repeatedChargeForSameRideReturnsExistingPayment() {
        when(rideClient.getRide("Bearer token", "ride-1")).thenReturn(completedRide);
        Payment prior = new Payment();
        prior.setId("payment-existing");
        prior.setRideId("ride-1");
        prior.setPassengerAccountId(passenger.id());
        prior.setCurrency("LKR");
        prior.setStatus(PaymentStatus.SUCCEEDED);
        prior.setTotalFare(new java.math.BigDecimal("900.00"));
        prior.setReceipt(new Payment.Receipt("RL-ABC123", java.time.Instant.now()));
        when(repository.findByRideId("ride-1")).thenReturn(Optional.of(prior));

        var result = service.charge(new ChargeRequest("ride-1", false), "Bearer token", passenger);
        assertEquals("payment-existing", result.id());
        verify(repository, never()).save(any(Payment.class));
    }
}
