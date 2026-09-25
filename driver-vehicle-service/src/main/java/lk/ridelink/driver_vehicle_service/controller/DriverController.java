package lk.ridelink.driver_vehicle_service.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.ridelink.driver_vehicle_service.client.AccountIdentityClient.AccountIdentity;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.AvailabilityRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.DriverProfileResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.EligibleDriverResponse;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.LocationRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.OperationalProfileRequest;
import lk.ridelink.driver_vehicle_service.dto.DriverDtos.VehicleRequest;
import lk.ridelink.driver_vehicle_service.service.DriverProfileService;

@Validated
@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers", description = "Driver operations and eligible-driver lookup")
public class DriverController {

    private final DriverProfileService driverProfileService;

    public DriverController(DriverProfileService driverProfileService) {
        this.driverProfileService = driverProfileService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "View my driver profile",
            security = @SecurityRequirement(name = "bearerAuth"))
    public DriverProfileResponse getMyProfile(
            @AuthenticationPrincipal AccountIdentity account) {
        return driverProfileService.getMyProfile(account.id());
    }

    @PutMapping("/me/profile")
    @Operation(
            summary = "Create or update my operational profile and service area",
            security = @SecurityRequirement(name = "bearerAuth"))
    public DriverProfileResponse saveOperationalProfile(
            @AuthenticationPrincipal AccountIdentity account,
            @Valid @RequestBody OperationalProfileRequest request) {
        return driverProfileService.saveOperationalProfile(account.id(), request);
    }

    @PutMapping("/me/vehicle")
    @Operation(
            summary = "Add or update my vehicle",
            security = @SecurityRequirement(name = "bearerAuth"))
    public DriverProfileResponse updateVehicle(
            @AuthenticationPrincipal AccountIdentity account,
            @Valid @RequestBody VehicleRequest request) {
        return driverProfileService.updateVehicle(account.id(), request);
    }

    @PutMapping("/me/location")
    @Operation(
            summary = "Update my simulated current location",
            security = @SecurityRequirement(name = "bearerAuth"))
    public DriverProfileResponse updateLocation(
            @AuthenticationPrincipal AccountIdentity account,
            @Valid @RequestBody LocationRequest request) {
        return driverProfileService.updateLocation(account.id(), request);
    }

    @PatchMapping("/me/availability")
    @Operation(
            summary = "Change my availability",
            security = @SecurityRequirement(name = "bearerAuth"))
    public DriverProfileResponse updateAvailability(
            @AuthenticationPrincipal AccountIdentity account,
            @Valid @RequestBody AvailabilityRequest request) {
        return driverProfileService.updateAvailability(account.id(), request);
    }

    @GetMapping("/eligible")
    @Operation(
            summary = "Find available drivers eligible for a pickup",
            description = """
                    Returns drivers who are marked AVAILABLE, have a complete profile
                    and vehicle, have a current location inside their service area,
                    serve the pickup point, and are within maxDistanceKm.
                    Results are sorted by distance.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"))
    public List<EligibleDriverResponse> findEligibleDrivers(
            @Parameter(description = "Pickup latitude", example = "6.9271")
            @RequestParam(name = "pickupLatitude") double pickupLatitude,
            @Parameter(description = "Pickup longitude", example = "79.8612")
            @RequestParam(name = "pickupLongitude") double pickupLongitude,
            @Parameter(description = "Maximum driver distance in kilometres", example = "10")
            @RequestParam(name = "maxDistanceKm", defaultValue = "10") double maxDistanceKm) {
        return driverProfileService.findEligibleDrivers(
                pickupLatitude,
                pickupLongitude,
                maxDistanceKm);
    }
}