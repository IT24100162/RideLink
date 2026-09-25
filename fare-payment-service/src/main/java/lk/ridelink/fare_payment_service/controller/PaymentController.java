package lk.ridelink.fare_payment_service.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lk.ridelink.fare_payment_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.fare_payment_service.dto.PaymentDtos;
import lk.ridelink.fare_payment_service.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Calculate final fare and record one simulated payment for a completed ride")
    public PaymentDtos.PaymentResponse charge(@Valid @RequestBody PaymentDtos.ChargeRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.charge(request, bearerToken, actor);
    }

    @GetMapping("/me")
    public List<PaymentDtos.PaymentResponse> mine(@AuthenticationPrincipal AccountIdentity actor) {
        return service.getMine(actor);
    }

    @GetMapping("/by-ride/{rideId}")
    public PaymentDtos.PaymentResponse byRide(@PathVariable String rideId,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.getByRideId(rideId, actor);
    }

    @GetMapping("/{paymentId}/receipt")
    public PaymentDtos.ReceiptResponse receipt(@PathVariable String paymentId,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.getReceipt(paymentId, actor);
    }

    @GetMapping("/{paymentId}")
    public PaymentDtos.PaymentResponse get(@PathVariable String paymentId,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.getById(paymentId, actor);
    }
}
