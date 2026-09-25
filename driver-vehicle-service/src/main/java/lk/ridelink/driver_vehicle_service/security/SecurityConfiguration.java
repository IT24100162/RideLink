package lk.ridelink.driver_vehicle_service.security;

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

    private final AccountAuthenticationFilter accountAuthenticationFilter;

    public SecurityConfiguration(
            AccountAuthenticationFilter accountAuthenticationFilter) {
        this.accountAuthenticationFilter = accountAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/error")
                    .permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/drivers/eligible")
                    .hasAnyRole("PASSENGER", "ADMIN")
                .requestMatchers(
                        "/api/v1/drivers/me",
                        "/api/v1/drivers/me/**")
                    .hasRole("DRIVER")
                .anyRequest().authenticated())
            .addFilterBefore(
                    accountAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}