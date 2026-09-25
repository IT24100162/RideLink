package lk.ridelink.fare_payment_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoIndexConfiguration {
    @Bean
    CommandLineRunner paymentIndexes(MongoTemplate mongo) {
        return args -> {
            mongo.indexOps("payments").ensureIndex(new Index().on("rideId", Sort.Direction.ASC).unique());
            mongo.indexOps("payments").ensureIndex(new Index().on("passengerAccountId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
        };
    }
}
