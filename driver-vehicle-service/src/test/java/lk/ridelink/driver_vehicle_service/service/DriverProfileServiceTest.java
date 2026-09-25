package lk.ridelink.driver_vehicle_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.driver_vehicle_service.dto.DriverDtos.AvailabilityRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.EligibleDriverResponse;
import lk.ridelink.driver_vehicle_service.model.DriverAvailability;
import lk.ridelink.driver_vehicle_service.model.DriverProfile;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.Location;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.ServiceArea;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.Vehicle;
import lk.ridelink.driver_vehicle_service.model.VehicleType;
import lk.ridelink.driver_vehicle_service.repository.DriverProfileRepository;

@ExtendWith(MockitoExtension.class)
class DriverProfileServiceTest {

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @InjectMocks
    private DriverProfileService driverProfileService;

    @Test
    void driverCannotBecomeAvailableUntilProfileIsComplete() {
        DriverProfile incompleteProfile = new DriverProfile();
        incompleteProfile.setAccountId("driver-123");

        when(driverProfileRepository.findByAccountId("driver-123"))
                .thenReturn(Optional.of(incompleteProfile));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> driverProfileService.updateAvailability(
                        "driver-123",
                        new AvailabilityRequest(DriverAvailability.AVAILABLE)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void driverCannotSetOnTripAvailabilityDirectly() {
        DriverProfile profile = new DriverProfile();
        profile.setAccountId("driver-123");

        when(driverProfileRepository.findByAccountId("driver-123"))
                .thenReturn(Optional.of(profile));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> driverProfileService.updateAvailability(
                        "driver-123",
                        new AvailabilityRequest(DriverAvailability.ON_TRIP)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void findsAvailableDriversWithinServiceAreaAndDistance() {
        DriverProfile profile = eligibleProfile(
                "driver-123",
                new Location(6.9271, 79.8612),
                new Location(6.9271, 79.8612),
                20);

        when(driverProfileRepository.findByAvailability(DriverAvailability.AVAILABLE))
                .thenReturn(List.of(profile));

        List<EligibleDriverResponse> results =
                driverProfileService.findEligibleDrivers(
                        6.9271,
                        79.8612,
                        10);

        assertEquals(1, results.size());
        assertEquals("driver-123", results.getFirst().driverAccountId());
        assertEquals(0.0, results.getFirst().distanceKm());
    }

    @Test
    void excludesDriverOutsideMaximumPickupDistance() {
        DriverProfile profile = eligibleProfile(
                "driver-456",
                new Location(7.2906, 80.6337),
                new Location(6.9271, 79.8612),
                100);

        when(driverProfileRepository.findByAvailability(DriverAvailability.AVAILABLE))
                .thenReturn(List.of(profile));

        List<EligibleDriverResponse> results =
                driverProfileService.findEligibleDrivers(
                        6.9271,
                        79.8612,
                        5);

        assertEquals(0, results.size());
    }

    @Test
    void returnsEmptyListWhenThereAreNoAvailableDrivers() {
        when(driverProfileRepository.findByAvailability(DriverAvailability.AVAILABLE))
                .thenReturn(List.of());

        List<EligibleDriverResponse> results =
                driverProfileService.findEligibleDrivers(
                        6.9271,
                        79.8612,
                        10);

        assertEquals(0, results.size());
    }

    private DriverProfile eligibleProfile(
            String accountId,
            Location currentLocation,
            Location serviceAreaCenter,
            double serviceAreaRadiusKm) {

        DriverProfile profile = new DriverProfile();
        profile.setId("profile-" + accountId);
        profile.setAccountId(accountId);
        profile.setDriverLicenseNumber("B1234567");
        profile.setLicenseExpiryDate(LocalDate.now().plusYears(2));
        profile.setAvailability(DriverAvailability.AVAILABLE);
        profile.setCurrentLocation(currentLocation);
        profile.setServiceArea(new ServiceArea(
                serviceAreaCenter,
                serviceAreaRadiusKm));
        profile.setVehicle(new Vehicle(
                "ABC-1234",
                VehicleType.SEDAN,
                "Toyota",
                "Corolla",
                2022,
                "White",
                4));

        return profile;
    }
}