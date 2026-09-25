package lk.ridelink.ride_management_service.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class DriverAvailabilityClient {

    private final RestClient restClient;

    public DriverAvailabilityClient(
            @Value("${app.driver-service.base-url}") String driverServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(driverServiceBaseUrl).build();
    }

    public List<EligibleDriver> findEligibleDrivers(
            String bearerToken,
            double pickupLatitude,
            double pickupLongitude,
            double maxDistanceKm) {
        try {
            EligibleDriver[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/drivers/eligible")
                            .queryParam("pickupLatitude", pickupLatitude)
                            .queryParam("pickupLongitude", pickupLongitude)
                            .queryParam("maxDistanceKm", maxDistanceKm)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(EligibleDriver[].class);

            return response == null ? List.of() : Arrays.asList(response);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver & Vehicle Service is unavailable");
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Driver & Vehicle Service rejected or failed the eligibility request");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EligibleDriver(String driverAccountId, Double distanceKm) {
    }
}
