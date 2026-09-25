package lk.ridelink.driver_vehicle_service.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.filter.OncePerRequestFilter;

import lk.ridelink.driver_vehicle_service.client.AccountIdentityClient;
import lk.ridelink.driver_vehicle_service.client.AccountIdentityClient.AccountIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AccountAuthenticationFilter extends OncePerRequestFilter {

    private final AccountIdentityClient accountIdentityClient;

    public AccountAuthenticationFilter(AccountIdentityClient accountIdentityClient) {
        this.accountIdentityClient = accountIdentityClient;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AccountIdentity identity =
                    accountIdentityClient.getCurrentAccount(authorization);

            if (identity == null
                    || identity.id() == null
                    || identity.role() == null
                    || !"ACTIVE".equalsIgnoreCase(identity.status())) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Account could not be authenticated");
                return;
            }

            String role = identity.role().toUpperCase(Locale.ROOT);

            var authentication = new UsernamePasswordAuthenticationToken(
                    identity,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (RestClientResponseException exception) {
            int upstreamStatus = exception.getStatusCode().value();

            if (upstreamStatus == HttpServletResponse.SC_UNAUTHORIZED
                    || upstreamStatus == HttpServletResponse.SC_FORBIDDEN) {
                writeError(response, upstreamStatus,
                        "Account Service rejected the bearer token");
            } else {
                writeError(response, HttpServletResponse.SC_BAD_GATEWAY,
                        "Account Service returned an error");
            }
        } catch (ResourceAccessException exception) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Account Service is unavailable");
        }
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":" + status
                        + ",\"error\":\"Authentication error\""
                        + ",\"message\":\"" + message + "\"}");
    }
}