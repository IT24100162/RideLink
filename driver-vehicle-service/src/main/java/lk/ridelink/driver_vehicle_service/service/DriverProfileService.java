package lk.ridelink.driver_vehicle_service.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lk.ridelink.driver_vehicle_service.dto.DriverDtos.AvailabilityRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.CoordinateResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.DriverProfileResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.EligibleDriverResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.OperationalProfileRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.ServiceAreaResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.VehicleRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.VehicleResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.LocationRequest;
import lk.ridelink.driver_vehicle_service.model.DriverAvailability;
import lk.ridelink.driver_vehicle_service.model.DriverProfile;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.Location;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.ServiceArea;
import lk.ridelink.driver_vehicle_service.model.DriverProfile.Vehicle;
import lk.ridelink.driver_vehicle_service.repository.DriverProfileRepository;

@Service
public class DriverProfileService {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final DriverProfileRepository driverProfileRepository;

    public DriverProfileService(DriverProfileRepository driverProfileRepository) {
        this.driverProfileRepository = driverProfileRepository;
    }

    public DriverProfileResponse getMyProfile(String accountId) {
        return toResponse(findByAccountId(accountId));
    }

    public DriverProfileResponse saveOperationalProfile(
            String accountId,
            OperationalProfileRequest request) {

        Instant now = Instant.now();

        DriverProfile profile = driverProfileRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    DriverProfile newProfile = new DriverProfile();
                    newProfile.setAccountId(accountId);
                    newProfile.setCreatedAt(now);
                    return newProfile;
                });

        profile.setDriverLicenseNumber(request.driverLicenseNumber().trim());
        profile.setLicenseExpiryDate(request.licenseExpiryDate());
        profile.setServiceArea(new ServiceArea(
                new Location(
                        request.serviceArea().center().latitude(),
                        request.serviceArea().center().longitude()),
                request.serviceArea().radiusKm()));
        profile.setAvailability(DriverAvailability.UNAVAILABLE);
        profile.setUpdatedAt(now);

        return toResponse(driverProfileRepository.save(profile));
    }

    public DriverProfileResponse updateVehicle(
            String accountId,
            VehicleRequest request) {

        DriverProfile profile = findByAccountId(accountId);

        profile.setVehicle(new Vehicle(
                request.registrationNumber().trim().toUpperCase(),
                request.type(),
                request.make().trim(),
                request.model().trim(),
                request.year(),
                request.color().trim(),
                request.seatingCapacity()));

        // Require the driver to explicitly become available after changing vehicle details.
        profile.setAvailability(DriverAvailability.UNAVAILABLE);
        profile.setUpdatedAt(Instant.now());

        return toResponse(driverProfileRepository.save(profile));
    }

    public DriverProfileResponse updateLocation(
            String accountId,
            LocationRequest request) {

        DriverProfile profile = findByAccountId(accountId);
        profile.setCurrentLocation(new Location(
                request.latitude(),
                request.longitude()));
        profile.setUpdatedAt(Instant.now());

        return toResponse(driverProfileRepository.save(profile));
    }

    public DriverProfileResponse updateAvailability(
            String accountId,
            AvailabilityRequest request) {

        DriverProfile profile = findByAccountId(accountId);
        DriverAvailability requestedAvailability = request.availability();

        if (requestedAvailability == DriverAvailability.ON_TRIP) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ON_TRIP is controlled by the Ride Service");
        }

        if (requestedAvailability == DriverAvailability.AVAILABLE
                && !isReadyAndInsideServiceArea(profile)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Complete the driver profile, vehicle, service area, and location "
                            + "and use a valid license before becoming available");
        }

        profile.setAvailability(requestedAvailability);
        profile.setUpdatedAt(Instant.now());

        return toResponse(driverProfileRepository.save(profile));
    }

    public List<EligibleDriverResponse> findEligibleDrivers(
            double pickupLatitude,
            double pickupLongitude,
            double maxDistanceKm) {

        validateCoordinate(pickupLatitude, pickupLongitude);

        if (!Double.isFinite(maxDistanceKm)
                || maxDistanceKm <= 0
                || maxDistanceKm > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "maxDistanceKm must be greater than 0 and no more than 200");
        }

        Location pickup = new Location(pickupLatitude, pickupLongitude);

        return driverProfileRepository.findByAvailability(DriverAvailability.AVAILABLE)
                .stream()
                .filter(this::isReadyAndInsideServiceArea)
                .filter(profile -> isPickupInsideServiceArea(pickup, profile))
                .map(profile -> new Candidate(
                        profile,
                        distanceKm(pickup, profile.getCurrentLocation())))
                .filter(candidate -> candidate.distanceKm() <= maxDistanceKm)
                .sorted(Comparator.comparingDouble(Candidate::distanceKm))
                .map(candidate -> new EligibleDriverResponse(
                        candidate.profile().getAccountId(),
                        toVehicleResponse(candidate.profile().getVehicle()),
                        toCoordinateResponse(candidate.profile().getCurrentLocation()),
                        roundToTwoDecimals(candidate.distanceKm())))
                .toList();
    }

    private DriverProfile findByAccountId(String accountId) {
        return driverProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver operational profile was not found"));
    }

    private boolean isReadyAndInsideServiceArea(DriverProfile profile) {
        if (profile.getDriverLicenseNumber() == null
                || profile.getDriverLicenseNumber().isBlank()
                || profile.getLicenseExpiryDate() == null
                || profile.getLicenseExpiryDate().isBefore(LocalDate.now())
                || profile.getServiceArea() == null
                || profile.getServiceArea().getCenter() == null
                || profile.getCurrentLocation() == null
                || profile.getVehicle() == null) {
            return false;
        }

        return distanceKm(
                profile.getServiceArea().getCenter(),
                profile.getCurrentLocation()) <= profile.getServiceArea().getRadiusKm();
    }

    private boolean isPickupInsideServiceArea(Location pickup, DriverProfile profile) {
        ServiceArea area = profile.getServiceArea();

        return distanceKm(area.getCenter(), pickup) <= area.getRadiusKm();
    }

    private double distanceKm(Location first, Location second) {
        double latitude1 = Math.toRadians(first.getLatitude());
        double latitude2 = Math.toRadians(second.getLatitude());
        double latitudeDifference = Math.toRadians(
                second.getLatitude() - first.getLatitude());
        double longitudeDifference = Math.toRadians(
                second.getLongitude() - first.getLongitude());

        double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);

        return 2 * EARTH_RADIUS_KM
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void validateCoordinate(double latitude, double longitude) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude or longitude is outside the valid range");
        }
    }

    private DriverProfileResponse toResponse(DriverProfile profile) {
        return new DriverProfileResponse(
                profile.getId(),
                profile.getAccountId(),
                profile.getDriverLicenseNumber(),
                profile.getLicenseExpiryDate(),
                profile.getAvailability(),
                toServiceAreaResponse(profile.getServiceArea()),
                toCoordinateResponse(profile.getCurrentLocation()),
                toVehicleResponse(profile.getVehicle()),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }

    private ServiceAreaResponse toServiceAreaResponse(ServiceArea serviceArea) {
        if (serviceArea == null) {
            return null;
        }

        return new ServiceAreaResponse(
                toCoordinateResponse(serviceArea.getCenter()),
                serviceArea.getRadiusKm());
    }

    private CoordinateResponse toCoordinateResponse(Location location) {
        if (location == null) {
            return null;
        }

        return new CoordinateResponse(
                location.getLatitude(),
                location.getLongitude());
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return new VehicleResponse(
                vehicle.getRegistrationNumber(),
                vehicle.getType(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getSeatingCapacity());
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Candidate(DriverProfile profile, double distanceKm) {
    }
}