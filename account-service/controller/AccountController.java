package lk.ridelink.account_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lk.ridelink.account_service.dto.ApiDtos.AccountResponse;
import lk.ridelink.account_service.dto.ApiDtos.EmailChangeRequest;
import lk.ridelink.account_service.dto.ApiDtos.PasswordChangeRequest;
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
        return accountService.getMyProfile(email);
    }

    @PatchMapping("/me")
    public AccountResponse updateMyProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateMyProfile(email, request);
    }

    @PatchMapping("/me/email")
    public AccountResponse changeMyEmail(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody EmailChangeRequest request) {
        return accountService.changeMyEmail(email, request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PasswordChangeRequest request) {
        accountService.changeMyPassword(email, request);
    }

    @DeleteMapping("/me")
    public AccountResponse closeMyAccount(
            @AuthenticationPrincipal String email,
            @RequestParam @NotBlank String currentPassword) {
        return accountService.closeMyAccount(email, currentPassword);
    }
}