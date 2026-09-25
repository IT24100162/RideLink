package lk.ridelink.driver_vehicle_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import lk.ridelink.driver_vehicle_service.model.DriverAvailability;
import lk.ridelink.driver_vehicle_service.model.DriverProfile;

public interface DriverProfileRepository extends MongoRepository<DriverProfile, String> {

    Optional<DriverProfile> findByAccountId(String accountId);

    List<DriverProfile> findByAvailability(DriverAvailability availability);
}