package lk.ridelink.fare_payment_service.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.fare_payment_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.fare_payment_service.client.RideClient;
import lk.ridelink.fare_payment_service.client.RideClient.RideSnapshot;
import lk.ridelink.fare_payment_service.dto.PaymentDtos;
import lk.ridelink.fare_payment_service.model.Payment;
import lk.ridelink.fare_payment_service.model.PaymentStatus;
import lk.ridelink.fare_payment_service.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final RideClient rideClient;
    private final FareCalculator fareCalculator;

    public PaymentService(PaymentRepository repository, RideClient rideClient, FareCalculator fareCalculator) {
        this.repository = repository;
        this.rideClient = rideClient;
        this.fareCalculator = fareCalculator;
    }

    public PaymentDtos.FareEstimateResponse estimate(PaymentDtos.FareEstimateRequest request) {
        FareCalculator.FareQuote quote = fareCalculator.calculate(
                request.pickup().latitude(), request.pickup().longitude(),
                request.destination().latitude(), request.destination().longitude());
        return new PaymentDtos.FareEstimateResponse("LKR", quote.straightLineDistanceKm(),
                quote.billableDistanceKm(), quote.baseFare(), quote.distanceFare(), quote.serviceFee(),
                quote.totalFare(), FareCalculator.PRICING_RULE, Instant.now());
    }

    public PaymentDtos.PaymentResponse charge(PaymentDtos.ChargeRequest request, String bearerToken,
            AccountIdentity actor) {
        if (!"PASSENGER".equalsIgnoreCase(actor.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a passenger can pay for a ride");
        }
        RideSnapshot ride = rideClient.getRide(bearerToken, request.rideId());
        if (ride == null || ride.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ride Service returned no ride data");
        }
        if (!actor.id().equals(ride.passengerAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the ride's passenger can pay");
        }
        if (!"COMPLETED".equalsIgnoreCase(ride.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is allowed only after ride completion");
        }

        var existing = repository.findByRideId(ride.id());
        if (existing.isPresent()) return toPaymentResponse(existing.get());

        if (ride.pickup() == null || ride.destination() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Completed ride has no pickup or destination coordinates");
        }
        FareCalculator.FareQuote quote = fareCalculator.calculate(ride.pickup().latitude(),
                ride.pickup().longitude(), ride.destination().latitude(), ride.destination().longitude());
        Instant now = Instant.now();
        Payment payment = new Payment();
        payment.setRideId(ride.id());
        payment.setPassengerAccountId(ride.passengerAccountId());
        payment.setDriverAccountId(ride.assignedDriverAccountId());
        payment.setCurrency("LKR");
        payment.setStraightLineDistanceKm(quote.straightLineDistanceKm());
        payment.setBillableDistanceKm(quote.billableDistanceKm());
        payment.setBaseFare(quote.baseFare());
        payment.setDistanceFare(quote.distanceFare());
        payment.setServiceFee(quote.serviceFee());
        payment.setTotalFare(quote.totalFare());
        payment.setCreatedAt(now);
        if (Boolean.TRUE.equals(request.simulateFailure())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureMessage("Simulated payment was declined; no real payment was attempted");
        } else {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setReceipt(new Payment.Receipt("RL-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 12).toUpperCase(), now));
        }
        try {
            return toPaymentResponse(repository.save(payment));
        } catch (DuplicateKeyException ex) {
            return repository.findByRideId(ride.id()).map(PaymentService::toPaymentResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for this ride"));
        }
    }

    public PaymentDtos.PaymentResponse getById(String id, AccountIdentity actor) {
        return toPaymentResponse(requireVisible(get(id), actor));
    }

    public PaymentDtos.PaymentResponse getByRideId(String rideId, AccountIdentity actor) {
        Payment payment = repository.findByRideId(rideId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for this ride"));
        return toPaymentResponse(requireVisible(payment, actor));
    }

    public PaymentDtos.ReceiptResponse getReceipt(String id, AccountIdentity actor) {
        Payment payment = requireVisible(get(id), actor);
        if (payment.getStatus() != PaymentStatus.SUCCEEDED || payment.getReceipt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Receipt is available only for a successful payment");
        }
        return new PaymentDtos.ReceiptResponse(payment.getReceipt().getReceiptNumber(), payment.getId(),
                payment.getRideId(), payment.getPassengerAccountId(), payment.getCurrency(),
                payment.getTotalFare(), payment.getStatus(), payment.getReceipt().getIssuedAt());
    }

    public List<PaymentDtos.PaymentResponse> getMine(AccountIdentity actor) {
        List<Payment> payments;
        if ("PASSENGER".equalsIgnoreCase(actor.role())) {
            payments = repository.findByPassengerAccountIdOrderByCreatedAtDesc(actor.id());
        } else if ("ADMIN".equalsIgnoreCase(actor.role())) {
            payments = repository.findAll();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only passengers and admins can view payment history");
        }
        return payments.stream().map(PaymentService::toPaymentResponse).toList();
    }

    private Payment get(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private static Payment requireVisible(Payment payment, AccountIdentity actor) {
        if (!"ADMIN".equalsIgnoreCase(actor.role()) && !actor.id().equals(payment.getPassengerAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the paying passenger or an admin can view this payment");
        }
        return payment;
    }

    private static PaymentDtos.PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentDtos.PaymentResponse(p.getId(), p.getRideId(), p.getPassengerAccountId(),
                p.getDriverAccountId(), p.getCurrency(), p.getStraightLineDistanceKm(),
                p.getBillableDistanceKm(), p.getBaseFare(), p.getDistanceFare(), p.getServiceFee(),
                p.getTotalFare(), p.getStatus(), p.getFailureMessage(),
                p.getReceipt() == null ? null : p.getReceipt().getReceiptNumber(), p.getCreatedAt());
    }
}
