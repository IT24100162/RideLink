package lk.ridelink.fare_payment_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final AccountAuthenticationFilter accountFilter;
    public SecurityConfiguration(AccountAuthenticationFilter accountFilter) { this.accountFilter = accountFilter; }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
            .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/fare-estimates").hasAnyRole("PASSENGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasRole("PASSENGER")
                .anyRequest().authenticated())
            .addFilterBefore(accountFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
