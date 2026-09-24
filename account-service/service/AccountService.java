package lk.ridelink.account_service.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.account_service.dto.ApiDtos.AccountResponse;
import lk.ridelink.account_service.dto.ApiDtos.AuthResponse;
import lk.ridelink.account_service.dto.ApiDtos.EmailChangeRequest;
import lk.ridelink.account_service.dto.ApiDtos.LoginRequest;
import lk.ridelink.account_service.dto.ApiDtos.PasswordChangeRequest;
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
                    HttpStatus.BAD_REQUEST, "ADMIN accounts cannot self-register");
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
                request.dateOfBirth(),
                normalizeOptional(request.profileImageUrl()),
                request.role(),
                AccountStatus.ACTIVE,
                0,
                now,
                now);

        return authResponse(accountRepository.save(account));
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

    public AccountResponse getMyProfile(String email) {
        return toResponse(findActiveAccountByEmail(email));
    }

    public AccountResponse updateMyProfile(
            String email,
            ProfileUpdateRequest request) {
        Account account = findActiveAccountByEmail(email);

        if (request.fullName() == null
                && request.phone() == null
                && request.dateOfBirth() == null
                && request.profileImageUrl() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provide at least one profile field to update");
        }

        if (request.fullName() != null) {
            if (request.fullName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Full name cannot be blank");
            }
            account.setFullName(request.fullName().trim());
        }
        if (request.phone() != null) {
            if (request.phone().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Phone cannot be blank");
            }
            account.setPhone(request.phone().trim());
        }
        if (request.dateOfBirth() != null) {
            account.setDateOfBirth(request.dateOfBirth());
        }
        if (request.profileImageUrl() != null) {
            account.setProfileImageUrl(normalizeOptional(request.profileImageUrl()));
        }

        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse changeMyEmail(String email, EmailChangeRequest request) {
        Account account = findActiveAccountByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        String newEmail = normalizeEmail(request.newEmail());
        if (!newEmail.equals(account.getEmail()) && accountRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "An account with this email already exists");
        }

        account.setEmail(newEmail);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public void changeMyPassword(String email, PasswordChangeRequest request) {
        Account account = findActiveAccountByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);
    }

    public AccountResponse closeMyAccount(String email, String currentPassword) {
        Account account = findActiveAccountByEmail(email);

        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        return closeAccount(account);
    }

    public Page<AccountResponse> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toResponse);
    }

    public AccountResponse getAccount(String id) {
        return toResponse(findAccountById(id));
    }

    public AccountResponse changeRole(String id, AccountRole role) {
        Account account = findAccountById(id);
        account.setRole(role);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse changeStatus(String id, AccountStatus status) {
        Account account = findAccountById(id);
        account.setStatus(status);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse closeAccountByAdmin(String id) {
        return closeAccount(findAccountById(id));
    }

    private AccountResponse closeAccount(Account account) {
        account.setStatus(AccountStatus.CLOSED);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());
        return toResponse(accountRepository.save(account));
    }

    private Account findActiveAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This account is not active");
        }
        return account;
    }

    private Account findAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AuthResponse authResponse(Account account) {
        return new AuthResponse(
                jwtService.generateToken(account.getEmail(), account.getTokenVersion()),
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
                account.getDateOfBirth(),
                account.getProfileImageUrl(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}