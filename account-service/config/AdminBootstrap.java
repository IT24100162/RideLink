package lk.ridelink.account_service.config;

import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lk.ridelink.account_service.model.Account;
import lk.ridelink.account_service.model.AccountRole;
import lk.ridelink.account_service.model.AccountStatus;
import lk.ridelink.account_service.repository.AccountRepository;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createBootstrapAdmin(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password) {

        return args -> {
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                return;
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (accountRepository.existsByEmail(normalizedEmail)) {
                return;
            }

            Instant now = Instant.now();
            Account admin = new Account(
                    "RideLink Administrator",
                    normalizedEmail,
                    passwordEncoder.encode(password),
                    "N/A",
                    AccountRole.ADMIN,
                    AccountStatus.ACTIVE,
                    now,
                    now);

            accountRepository.save(admin);
        };
    }
}