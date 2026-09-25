package lk.ridelink.fare_payment_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class RideClient {
    private final RestClient client;
    public RideClient(@Value("${app.ride-service.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public RideSnapshot getRide(String token, String rideId) {
        try {
            return client.get().uri("/api/v1/rides/{id}", rideId)
                    .header(HttpHeaders.AUTHORIZATION, token).retrieve().body(RideSnapshot.class);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ride Management Service is unavailable");
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            if (status >= 400 && status < 500) {
                throw new ResponseStatusException(HttpStatus.valueOf(status), "Ride Management Service rejected the ride lookup");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ride Management Service returned an error");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RideSnapshot(String id, String passengerAccountId, String assignedDriverAccountId,
            Place pickup, Place destination, String status) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Place(String address, double latitude, double longitude) {}
}
