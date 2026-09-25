package lk.ridelink.fare_payment_service.security;

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
import lk.ridelink.fare_payment_service.client.AccountIdentityClient;
import lk.ridelink.fare_payment_service.client.AccountIdentityClient.AccountIdentity;

@Component
public class AccountAuthenticationFilter extends OncePerRequestFilter {
    private final AccountIdentityClient accountClient;
    public AccountAuthenticationFilter(AccountIdentityClient accountClient) { this.accountClient = accountClient; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs") || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) { chain.doFilter(request, response); return; }
        try {
            AccountIdentity identity = accountClient.getCurrentAccount(token);
            if (identity == null || identity.id() == null || identity.role() == null
                    || !"ACTIVE".equalsIgnoreCase(identity.status())) {
                writeError(response, 401, "Account could not be authenticated"); return;
            }
            var auth = new UsernamePasswordAuthenticationToken(identity, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + identity.role().toUpperCase(Locale.ROOT))));
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            writeError(response, status == 401 || status == 403 ? status : 502,
                    "Account Service rejected the token or returned an error");
        } catch (ResourceAccessException ex) {
            writeError(response, 503, "Account Service is unavailable");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"Authentication error\",\"message\":\"" + message + "\"}");
    }
}
