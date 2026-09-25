package lk.ridelink.account_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lk.ridelink.account_service.dto.ApiDtos.AuthResponse;
import lk.ridelink.account_service.dto.ApiDtos.LoginRequest;
import lk.ridelink.account_service.dto.ApiDtos.RegisterRequest;
import lk.ridelink.account_service.service.AccountService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }
}