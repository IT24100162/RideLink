package lk.ridelink.fare_payment_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import lk.ridelink.fare_payment_service.model.Payment;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRideId(String rideId);
    List<Payment> findByPassengerAccountIdOrderByCreatedAtDesc(String passengerAccountId);
}
