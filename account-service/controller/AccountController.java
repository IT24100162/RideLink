package lk.ridelink.account_service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lk.ridelink.account_service.dto.ApiDtos.AccountResponse;
import lk.ridelink.account_service.dto.ApiDtos.ProfileUpdateRequest;
import lk.ridelink.account_service.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public AccountResponse getMyProfile(@AuthenticationPrincipal String email) {
        return accountService.getProfile(email);
    }

    @PatchMapping("/me")
    public AccountResponse updateMyProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateProfile(email, request);
    }
}