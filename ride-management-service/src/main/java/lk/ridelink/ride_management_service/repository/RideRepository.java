package lk.ridelink.ride_management_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import lk.ridelink.ride_management_service.model.Ride;

public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerAccountIdOrderByCreatedAtDesc(String passengerAccountId);

    List<Ride> findByAssignedDriverAccountIdOrderByCreatedAtDesc(String driverAccountId);
}
