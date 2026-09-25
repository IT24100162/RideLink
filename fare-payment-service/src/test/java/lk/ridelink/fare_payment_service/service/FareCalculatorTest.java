package lk.ridelink.fare_payment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class FareCalculatorTest {
    private final FareCalculator calculator = new FareCalculator();

    @Test
    void zeroDistanceUsesOneBillableKilometreAndDocumentedFixedFees() {
        var quote = calculator.calculate(6.9271, 79.8612, 6.9271, 79.8612);
        assertEquals(new BigDecimal("1.000"), quote.billableDistanceKm());
        assertEquals(new BigDecimal("300.00"), quote.baseFare());
        assertEquals(new BigDecimal("120.00"), quote.distanceFare());
        assertEquals(new BigDecimal("50.00"), quote.serviceFee());
        assertEquals(new BigDecimal("470.00"), quote.totalFare());
    }

    @Test
    void longerTripHasHigherDistanceAndFare() {
        var shortTrip = calculator.calculate(6.9344, 79.8428, 6.8941, 79.8560);
        var longTrip = calculator.calculate(6.9271, 79.8612, 7.2906, 80.6337);
        assertTrue(longTrip.billableDistanceKm().compareTo(shortTrip.billableDistanceKm()) > 0);
        assertTrue(longTrip.totalFare().compareTo(shortTrip.totalFare()) > 0);
    }
}
