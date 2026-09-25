package lk.ridelink.fare_payment_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class AccountIdentityClient {
    private final RestClient client;
    public AccountIdentityClient(@Value("${app.account-service.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }
    public AccountIdentity getCurrentAccount(String token) {
        return client.get().uri("/api/v1/accounts/me")
                .header(HttpHeaders.AUTHORIZATION, token).retrieve().body(AccountIdentity.class);
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountIdentity(String id, String email, String role, String status) {}
}
