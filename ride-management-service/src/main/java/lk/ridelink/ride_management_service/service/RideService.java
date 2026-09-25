package lk.ridelink.ride_management_service.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.ride_management_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.ride_management_service.client.DriverAvailabilityClient;
import lk.ridelink.ride_management_service.client.DriverAvailabilityClient.EligibleDriver;
import lk.ridelink.ride_management_service.dto.RideDtos;
import lk.ridelink.ride_management_service.model.Ride;
import lk.ridelink.ride_management_service.model.RideStatus;
import lk.ridelink.ride_management_service.repository.RideRepository;

@Service
public class RideService {
    private static final double MAX_ASSIGNMENT_DISTANCE_KM = 25.0;
    private final RideRepository repository;
    private final DriverAvailabilityClient driverClient;

    public RideService(RideRepository repository, DriverAvailabilityClient driverClient) {
        this.repository = repository;
        this.driverClient = driverClient;
    }

    public RideDtos.RideResponse create(RideDtos.CreateRideRequest request, AccountIdentity actor) {
        Ride ride = new Ride();
        ride.setPassengerAccountId(actor.id());
        ride.setPickup(toPlace(request.pickup()));
        ride.setDestination(toPlace(request.destination()));
        ride.setStatus(RideStatus.REQUESTED);
        ride.setCreatedAt(Instant.now());
        ride.setUpdatedAt(ride.getCreatedAt());
        return response(repository.save(ride));
    }

    public RideDtos.RideResponse assign(String id, String bearerToken, AccountIdentity actor) {
        Ride ride = ownedOrAdmin(id, actor);
        requireStatus(ride, RideStatus.REQUESTED);
        List<EligibleDriver> drivers = driverClient.findEligibleDrivers(bearerToken,
                ride.getPickup().getLatitude(), ride.getPickup().getLongitude(), MAX_ASSIGNMENT_DISTANCE_KM);
        if (drivers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No eligible available drivers were found");
        }
        EligibleDriver selected = drivers.getFirst();
        if (selected.driverAccountId() == null || selected.driverAccountId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Driver Service returned an invalid candidate");
        }
        ride.setAssignedDriverAccountId(selected.driverAccountId());
        ride.setAssignmentDistanceKm(selected.distanceKm());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        ride.setUpdatedAt(ride.getAssignedAt());
        return response(repository.save(ride));
    }

    public RideDtos.RideResponse accept(String id, AccountIdentity actor) {
        Ride ride = get(id);
        requireAssignedDriver(ride, actor);
        requireStatus(ride, RideStatus.ASSIGNED);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());
        return saveUpdated(ride);
    }

    public RideDtos.RideResponse start(String id, AccountIdentity actor) {
        Ride ride = get(id);
        requireAssignedDriver(ride, actor);
        requireStatus(ride, RideStatus.ACCEPTED);
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());
        return saveUpdated(ride);
    }

    public RideDtos.RideResponse complete(String id, AccountIdentity actor) {
        Ride ride = get(id);
        requireAssignedDriver(ride, actor);
        requireStatus(ride, RideStatus.IN_PROGRESS);
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());
        return saveUpdated(ride);
    }

    public RideDtos.RideResponse cancel(String id, RideDtos.CancelRideRequest request, AccountIdentity actor) {
        Ride ride = get(id);
        if (!isAdmin(actor) && !actor.id().equals(ride.getPassengerAccountId())
                && !actor.id().equals(ride.getAssignedDriverAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this ride");
        }
        if (ride.getStatus() != RideStatus.REQUESTED && ride.getStatus() != RideStatus.ASSIGNED
                && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride cannot be cancelled in its current status");
        }
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(request == null ? null : request.reason());
        ride.setCancelledAt(Instant.now());
        return saveUpdated(ride);
    }

    public RideDtos.RideResponse getById(String id, AccountIdentity actor) {
        Ride ride = get(id);
        if (!isAdmin(actor) && !actor.id().equals(ride.getPassengerAccountId())
                && !actor.id().equals(ride.getAssignedDriverAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this ride");
        }
        return response(ride);
    }

    public List<RideDtos.RideResponse> getMine(AccountIdentity actor) {
        List<Ride> rides = switch (actor.role().toUpperCase()) {
            case "PASSENGER" -> repository.findByPassengerAccountIdOrderByCreatedAtDesc(actor.id());
            case "DRIVER" -> repository.findByAssignedDriverAccountIdOrderByCreatedAtDesc(actor.id());
            case "ADMIN" -> repository.findAll();
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role cannot retrieve rides");
        };
        return rides.stream().map(RideService::response).toList();
    }

    private Ride ownedOrAdmin(String id, AccountIdentity actor) {
        Ride ride = get(id);
        if (!isAdmin(actor) && !actor.id().equals(ride.getPassengerAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requesting passenger or admin can assign this ride");
        }
        return ride;
    }

    private Ride get(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }

    private void requireAssignedDriver(Ride ride, AccountIdentity actor) {
        if (!actor.id().equals(ride.getAssignedDriverAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned driver can perform this action");
        }
    }

    private static void requireStatus(Ride ride, RideStatus expected) {
        if (ride.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid ride status transition from " + ride.getStatus() + "; expected " + expected);
        }
    }

    private RideDtos.RideResponse saveUpdated(Ride ride) {
        ride.setUpdatedAt(Instant.now());
        return response(repository.save(ride));
    }

    private static boolean isAdmin(AccountIdentity actor) { return "ADMIN".equalsIgnoreCase(actor.role()); }

    private static Ride.Place toPlace(RideDtos.PlaceRequest place) {
        return new Ride.Place(place.address().trim(), place.latitude(), place.longitude());
    }

    private static RideDtos.RideResponse response(Ride ride) {
        return new RideDtos.RideResponse(ride.getId(), ride.getPassengerAccountId(),
                ride.getAssignedDriverAccountId(), placeResponse(ride.getPickup()),
                placeResponse(ride.getDestination()), ride.getStatus(), ride.getAssignmentDistanceKm(),
                ride.getCancellationReason(), ride.getCreatedAt(), ride.getUpdatedAt(),
                ride.getAssignedAt(), ride.getAcceptedAt(), ride.getStartedAt(), ride.getCompletedAt(),
                ride.getCancelledAt());
    }

    private static RideDtos.PlaceResponse placeResponse(Ride.Place place) {
        return new RideDtos.PlaceResponse(place.getAddress(), place.getLatitude(), place.getLongitude());
    }
}
