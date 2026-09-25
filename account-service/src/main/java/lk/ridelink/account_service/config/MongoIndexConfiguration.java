package lk.ridelink.account_service.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import lk.ridelink.account_service.model.Account;

@Configuration
public class MongoIndexConfiguration {
    @Bean
    ApplicationRunner createAccountIndexes(MongoTemplate mongoTemplate) {
        return args -> mongoTemplate.indexOps(Account.class)
                .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
    }
}