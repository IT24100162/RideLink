package lk.ridelink.ride_management_service.security;

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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ridelink.ride_management_service.client.AccountIdentityClient;
import lk.ridelink.ride_management_service.client.AccountIdentityClient.AccountIdentity;

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

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AccountIdentity identity = accountIdentityClient.getCurrentAccount(bearerToken);
            if (identity == null || identity.id() == null || identity.role() == null
                    || !"ACTIVE".equalsIgnoreCase(identity.status())) {
                writeError(response, 401, "Account could not be authenticated");
                return;
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    identity,
                    null,
                    List.of(new SimpleGrantedAuthority(
                            "ROLE_" + identity.role().toUpperCase(Locale.ROOT))));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 401 || status == 403) {
                writeError(response, status, "Account Service rejected the bearer token");
            } else {
                writeError(response, 502, "Account Service returned an error");
            }
        } catch (ResourceAccessException exception) {
            writeError(response, 503, "Account Service is unavailable");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status
                + ",\"error\":\"Authentication error\",\"message\":\""
                + message + "\"}");
    }
}
