package lk.ridelink.fare_payment_service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lk.ridelink.fare_payment_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.fare_payment_service.dto.PaymentDtos;
import lk.ridelink.fare_payment_service.service.PaymentService;

@RestController
@RequestMapping("/api/v1/fare-estimates")
public class FareController {
    private final PaymentService service;
    public FareController(PaymentService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Estimate the fare for two coordinates", security = @SecurityRequirement(name = "bearerAuth"))
    public PaymentDtos.FareEstimateResponse estimate(@Valid @RequestBody PaymentDtos.FareEstimateRequest request,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.estimate(request);
    }
}
