package com.springboot.springbootwebflux;

import com.springboot.springbootwebflux.domain.Item;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class TemplateDatabaseLoader {

    @Bean
    CommandLineRunner initialize(MongoOperations mongo) {
        return args -> {
            mongo.save(new Item("alf clock", 19.99));
            mongo.save(new Item("smurf TV", 24.99));
        };
    }
}
