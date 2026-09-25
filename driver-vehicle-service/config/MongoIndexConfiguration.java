package lk.ridelink.driver_vehicle_service.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import lk.ridelink.driver_vehicle_service.model.DriverProfile;

@Configuration
public class MongoIndexConfiguration {


    @Bean
    ApplicationRunner createDriverProfileIndexes(MongoTemplate mongoTemplate) {
        return args -> mongoTemplate.indexOps(DriverProfile.class)
                .ensureIndex(new Index()
                        .on("accountId", Sort.Direction.ASC)
                        .unique()
                        .named("driver_account_id_unique"));
    }
}