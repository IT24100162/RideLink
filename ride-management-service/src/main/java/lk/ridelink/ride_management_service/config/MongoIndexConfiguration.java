package lk.ridelink.ride_management_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@Configuration
public class MongoIndexConfiguration {
    @Bean
    CommandLineRunner createRideIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.indexOps("rides").ensureIndex(
                    new Index().on("passengerAccountId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
            mongoTemplate.indexOps("rides").ensureIndex(
                    new Index().on("assignedDriverAccountId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
        };
    }
}
