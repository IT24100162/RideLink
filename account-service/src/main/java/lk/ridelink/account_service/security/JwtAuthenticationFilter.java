package lk.ridelink.account_service.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ridelink.account_service.model.Account;
import lk.ridelink.account_service.model.AccountStatus;
import lk.ridelink.account_service.repository.AccountRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AccountRepository accountRepository) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null
                && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtService.TokenClaims claims =
                        jwtService.parseToken(authorization.substring(7));

                Account account = accountRepository.findByEmail(claims.email()).orElse(null);

                if (account != null
                        && account.getStatus() == AccountStatus.ACTIVE
                        && account.getTokenVersion() == claims.version()) {
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    account.getEmail(), null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}