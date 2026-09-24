package lk.ridelink.account_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.account_service.dto.ApiDtos.LoginRequest;
import lk.ridelink.account_service.dto.ApiDtos.ProfileUpdateRequest;
import lk.ridelink.account_service.dto.ApiDtos.RegisterRequest;
import lk.ridelink.account_service.model.Account;
import lk.ridelink.account_service.model.AccountRole;
import lk.ridelink.account_service.model.AccountStatus;
import lk.ridelink.account_service.repository.AccountRepository;
import lk.ridelink.account_service.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountService accountService;

    private Account activePassenger;

    @BeforeEach
    void setUp() {
        activePassenger = new Account(
                "Test Passenger",
                "passenger@example.com",
                "hashed-password",
                "0771234567",
                AccountRole.PASSENGER,
                AccountStatus.ACTIVE,
                Instant.parse("2026-09-24T10:00:00Z"),
                Instant.parse("2026-09-24T10:00:00Z"));

        activePassenger.setId("account-123");
    }

    @Test
    void registerCreatesPassengerAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
                "Test Passenger",
                "Passenger@Example.com",
                "Passphrase123!",
                "0771234567",
                AccountRole.PASSENGER);

        when(accountRepository.existsByEmail("passenger@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passphrase123!")).thenReturn("hashed-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId("account-123");
            return saved;
        });
        when(jwtService.generateToken("passenger@example.com"))
                .thenReturn("test.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var result = accountService.register(request);

        assertEquals("test.jwt.token", result.accessToken());
        assertEquals("passenger@example.com", result.account().email());
        assertEquals(AccountRole.PASSENGER, result.account().role());
        assertEquals(AccountStatus.ACTIVE, result.account().status());
        assertNotNull(result.account().id());
        verify(passwordEncoder).encode("Passphrase123!");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void registerCreatesDriverAndReturnsDriverRole() {
        RegisterRequest request = new RegisterRequest(
                "Test Driver",
                "driver@example.com",
                "Passphrase123!",
                "0712345678",
                AccountRole.DRIVER);

        when(accountRepository.existsByEmail("driver@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passphrase123!")).thenReturn("hashed-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId("driver-123");
            return saved;
        });
        when(jwtService.generateToken("driver@example.com"))
                .thenReturn("driver.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var result = accountService.register(request);

        assertEquals("driver.jwt.token", result.accessToken());
        assertEquals("driver@example.com", result.account().email());
        assertEquals(AccountRole.DRIVER, result.account().role());
        assertEquals(AccountStatus.ACTIVE, result.account().status());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Test Passenger",
                "passenger@example.com",
                "Passphrase123!",
                "0771234567",
                AccountRole.PASSENGER);

        when(accountRepository.existsByEmail("passenger@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void registerDoesNotAllowSelfRegistrationAsAdmin() {
        RegisterRequest request = new RegisterRequest(
                "Someone",
                "someone@example.com",
                "Passphrase123!",
                "0771234567",
                AccountRole.ADMIN);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void loginReturnsTokenForActiveAccountWithCorrectPassword() {
        when(accountRepository.findByEmail("passenger@example.com"))
                .thenReturn(Optional.of(activePassenger));
        when(passwordEncoder.matches("Passphrase123!", "hashed-password"))
                .thenReturn(true);
        when(jwtService.generateToken("passenger@example.com"))
                .thenReturn("test.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var result = accountService.login(
                new LoginRequest("PASSENGER@example.com", "Passphrase123!"));

        assertEquals("test.jwt.token", result.accessToken());
        assertEquals("passenger@example.com", result.account().email());
        assertEquals(3600L, result.expiresInSeconds());
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(accountRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.login(
                        new LoginRequest("unknown@example.com", "Passphrase123!")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        when(accountRepository.findByEmail("passenger@example.com"))
                .thenReturn(Optional.of(activePassenger));
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.login(
                        new LoginRequest("passenger@example.com", "wrong-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginRejectsSuspendedAccount() {
        activePassenger.setStatus(AccountStatus.SUSPENDED);

        when(accountRepository.findByEmail("passenger@example.com"))
                .thenReturn(Optional.of(activePassenger));
        when(passwordEncoder.matches("Passphrase123!", "hashed-password"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.login(
                        new LoginRequest("passenger@example.com", "Passphrase123!")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updateProfileChangesNameAndPhone() {
        when(accountRepository.findByEmail("passenger@example.com"))
                .thenReturn(Optional.of(activePassenger));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = accountService.updateProfile(
                "passenger@example.com",
                new ProfileUpdateRequest("Updated Name", "0712345678"));

        assertEquals("Updated Name", result.fullName());
        assertEquals("0712345678", result.phone());
        assertEquals("passenger@example.com", result.email());
        verify(accountRepository).save(activePassenger);
    }
}