package lk.ridelink.account_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import lk.ridelink.account_service.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Account> findAll(Pageable pageable);
}