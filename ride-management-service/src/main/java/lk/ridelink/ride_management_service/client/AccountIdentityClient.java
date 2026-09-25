package lk.ridelink.ride_management_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class AccountIdentityClient {

    private final RestClient restClient;

    public AccountIdentityClient(
            @Value("${app.account-service.base-url}") String accountServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(accountServiceBaseUrl).build();
    }

    public AccountIdentity getCurrentAccount(String bearerToken) {
        return restClient.get()
                .uri("/api/v1/accounts/me")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(AccountIdentity.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountIdentity(String id, String email, String role, String status) {
    }
}
