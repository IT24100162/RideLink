package lk.ridelink.account_service.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.account_service.dto.ApiDtos.AccountResponse;
import lk.ridelink.account_service.dto.ApiDtos.AuthResponse;
import lk.ridelink.account_service.dto.ApiDtos.LoginRequest;
import lk.ridelink.account_service.dto.ApiDtos.ProfileUpdateRequest;
import lk.ridelink.account_service.dto.ApiDtos.RegisterRequest;
import lk.ridelink.account_service.model.Account;
import lk.ridelink.account_service.model.AccountRole;
import lk.ridelink.account_service.model.AccountStatus;
import lk.ridelink.account_service.repository.AccountRepository;
import lk.ridelink.account_service.security.JwtService;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (request.role() == AccountRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ADMIN accounts cannot be self-registered");
        }

        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "An account with this email already exists");
        }

        Instant now = Instant.now();
        Account account = new Account(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.phone().trim(),
                request.role(),
                AccountStatus.ACTIVE,
                now,
                now);

        Account saved = accountRepository.save(account);
        return authResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This account is not active");
        }

        return authResponse(account);
    }

    public AccountResponse getProfile(String email) {
        return toResponse(findByEmail(email));
    }

    public AccountResponse updateProfile(String email, ProfileUpdateRequest request) {
        Account account = findByEmail(email);
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone().trim());
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse changeStatus(String id, AccountStatus status) {
        Account account = findById(id);
        account.setStatus(status);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse changeRole(String id, AccountRole role) {
        Account account = findById(id);
        account.setRole(role);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public Account findByEmail(String email) {
        return accountRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));
    }

    private Account findById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AuthResponse authResponse(Account account) {
        return new AuthResponse(
                jwtService.generateToken(account.getEmail()),
                "Bearer",
                jwtService.getExpirationSeconds(),
                toResponse(account));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}