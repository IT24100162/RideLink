package lk.ridelink.ride_management_service.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lk.ridelink.ride_management_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.ride_management_service.dto.RideDtos;
import lk.ridelink.ride_management_service.service.RideService;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {
    private final RideService service;

    public RideController(RideService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideDtos.RideResponse create(@Valid @RequestBody RideDtos.CreateRideRequest request,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.create(request, actor);
    }

    @PostMapping("/{id}/assign")
    public RideDtos.RideResponse assign(@PathVariable String id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.assign(id, bearerToken, actor);
    }

    @PostMapping("/{id}/accept")
    public RideDtos.RideResponse accept(@PathVariable String id, @AuthenticationPrincipal AccountIdentity actor) {
        return service.accept(id, actor);
    }

    @PostMapping("/{id}/start")
    public RideDtos.RideResponse start(@PathVariable String id, @AuthenticationPrincipal AccountIdentity actor) {
        return service.start(id, actor);
    }

    @PostMapping("/{id}/complete")
    public RideDtos.RideResponse complete(@PathVariable String id, @AuthenticationPrincipal AccountIdentity actor) {
        return service.complete(id, actor);
    }

    @PostMapping("/{id}/cancel")
    public RideDtos.RideResponse cancel(@PathVariable String id,
            @Valid @RequestBody(required = false) RideDtos.CancelRideRequest request,
            @AuthenticationPrincipal AccountIdentity actor) {
        return service.cancel(id, request, actor);
    }

    @GetMapping("/{id}")
    public RideDtos.RideResponse get(@PathVariable String id, @AuthenticationPrincipal AccountIdentity actor) {
        return service.getById(id, actor);
    }

    @GetMapping("/me")
    public List<RideDtos.RideResponse> mine(@AuthenticationPrincipal AccountIdentity actor) {
        return service.getMine(actor);
    }
}
