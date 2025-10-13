package com.se100.bds.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = {
        "com.se100.bds.repositories.domains.customer",
        "com.se100.bds.repositories.domains.search"
})
public class MongoConfig {
}
