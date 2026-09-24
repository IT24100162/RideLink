package lk.ridelink.account_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    public Page<AccountResponse> listAccounts(
            @PageableDefault(size = 20) Pageable pageable) {
        return accountService.listAccounts(pageable);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable String id) {
        return accountService.getAccount(id);
    }

    @PatchMapping("/{id}/role")
    public AccountResponse changeRole(
            @PathVariable String id,
            @Valid @RequestBody RoleUpdateRequest request) {
        return accountService.changeRole(id, request.role());
    }

    @PatchMapping("/{id}/status")
    public AccountResponse changeStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return accountService.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public AccountResponse closeAccount(@PathVariable String id) {
        return accountService.closeAccountByAdmin(id);
    }
}