package lk.ridelink.account_service.controller;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lk.ridelink.account_service.dto.ApiDtos.AccountResponse;
import lk.ridelink.account_service.dto.ApiDtos.RoleUpdateRequest;
import lk.ridelink.account_service.dto.ApiDtos.StatusUpdateRequest;
import lk.ridelink.account_service.service.AccountService;

@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminAccountController {
    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping("/{id}/status")
    public AccountResponse changeStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return accountService.changeStatus(id, request.status());
    }

    @PatchMapping("/{id}/role")
    public AccountResponse changeRole(
            @PathVariable String id,
            @Valid @RequestBody RoleUpdateRequest request) {
        return accountService.changeRole(id, request.role());
    }
}