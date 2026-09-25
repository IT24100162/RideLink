package lk.ridelink.ride_management_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.ride_management_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.ride_management_service.client.DriverAvailabilityClient;
import lk.ridelink.ride_management_service.client.DriverAvailabilityClient.EligibleDriver;
import lk.ridelink.ride_management_service.dto.RideDtos;
import lk.ridelink.ride_management_service.model.Ride;
import lk.ridelink.ride_management_service.model.RideStatus;
import lk.ridelink.ride_management_service.repository.RideRepository;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {
    @Mock RideRepository repository;
    @Mock DriverAvailabilityClient driverClient;

    private RideService service;
    private AccountIdentity passenger;
    private AccountIdentity driver;
    private Ride ride;

    @BeforeEach
    void setUp() {
        service = new RideService(repository, driverClient);
        passenger = new AccountIdentity("passenger-1", "p@example.com", "PASSENGER", "ACTIVE");
        driver = new AccountIdentity("driver-1", "d@example.com", "DRIVER", "ACTIVE");
        ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerAccountId(passenger.id());
        ride.setPickup(new Ride.Place("Colombo", 6.9271, 79.8612));
        ride.setDestination(new Ride.Place("Kandy", 7.2906, 80.6337));
        ride.setStatus(RideStatus.REQUESTED);
        when(repository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride saved = invocation.getArgument(0);
            if (saved.getId() == null) saved.setId("ride-1");
            return saved;
        });
    }

    @Test
    void createStartsRequestedAndPersistsPickupAndDestination() {
        var result = service.create(request(), passenger);
        assertEquals(RideStatus.REQUESTED, result.status());
        assertEquals("passenger-1", result.passengerAccountId());
        assertEquals("Colombo", result.pickup().address());
        assertNotNull(result.createdAt());
        verify(repository).save(any(Ride.class));
    }

    @Test
    void assignmentChoosesFirstEligibleCandidate() {
        ride.setId("ride-1");
        when(repository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.findEligibleDrivers(anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(new EligibleDriver("driver-1", 2.4)));

        var result = service.assign("ride-1", "Bearer token", passenger);
        assertEquals(RideStatus.ASSIGNED, result.status());
        assertEquals("driver-1", result.assignedDriverAccountId());
        assertEquals(2.4, result.assignmentDistanceKm(), 0.001);
    }

    @Test
    void assignmentWithNoEligibleDriversReturnsConflictAndDoesNotAssign() {
        when(repository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.findEligibleDrivers(anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.assign("ride-1", "Bearer token", passenger));
        assertEquals(409, exception.getStatusCode().value());
        verify(repository, never()).save(ride);
    }

    @Test
    void assignedDriverCanCompleteValidLifecycle() {
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedDriverAccountId(driver.id());
        when(repository.findById("ride-1")).thenReturn(Optional.of(ride));

        assertEquals(RideStatus.ACCEPTED, service.accept("ride-1", driver).status());
        assertEquals(RideStatus.IN_PROGRESS, service.start("ride-1", driver).status());
        assertEquals(RideStatus.COMPLETED, service.complete("ride-1", driver).status());
    }

    @Test
    void cannotStartBeforeAccepting() {
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedDriverAccountId(driver.id());
        when(repository.findById("ride-1")).thenReturn(Optional.of(ride));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.start("ride-1", driver));
        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void unrelatedPassengerCannotReadRide() {
        when(repository.findById("ride-1")).thenReturn(Optional.of(ride));
        AccountIdentity stranger = new AccountIdentity("stranger", "s@example.com", "PASSENGER", "ACTIVE");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getById("ride-1", stranger));
        assertEquals(403, exception.getStatusCode().value());
    }

    private static RideDtos.CreateRideRequest request() {
        return new RideDtos.CreateRideRequest(
                new RideDtos.PlaceRequest("Colombo", 6.9271, 79.8612),
                new RideDtos.PlaceRequest("Kandy", 7.2906, 80.6337));
    }
}
