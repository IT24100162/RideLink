package lk.ridelink.fare_payment_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class FareCalculator {
    public static final String PRICING_RULE = "LKR 300 base + LKR 120 per billable km + LKR 50 service fee; billable km = max(1.000, Haversine distance x 1.25); no surge; round money to 2 decimals";
    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final BigDecimal BASE = new BigDecimal("300.00");
    private static final BigDecimal RATE_PER_KM = new BigDecimal("120.00");
    private static final BigDecimal SERVICE_FEE = new BigDecimal("50.00");

    public FareQuote calculate(double fromLat, double fromLon, double toLat, double toLon) {
        double lat1 = Math.toRadians(fromLat);
        double lat2 = Math.toRadians(toLat);
        double dLat = Math.toRadians(toLat - fromLat);
        double dLon = Math.toRadians(toLon - fromLon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double haversine = 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        BigDecimal straight = BigDecimal.valueOf(haversine).setScale(3, RoundingMode.HALF_UP);
        BigDecimal billable = BigDecimal.valueOf(Math.max(1.0, haversine * 1.25)).setScale(3, RoundingMode.HALF_UP);
        BigDecimal distanceFare = RATE_PER_KM.multiply(billable).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = BASE.add(distanceFare).add(SERVICE_FEE).setScale(2, RoundingMode.HALF_UP);
        return new FareQuote(straight, billable, BASE, distanceFare, SERVICE_FEE, total);
    }

    public record FareQuote(BigDecimal straightLineDistanceKm, BigDecimal billableDistanceKm,
            BigDecimal baseFare, BigDecimal distanceFare, BigDecimal serviceFee, BigDecimal totalFare) {}
}
