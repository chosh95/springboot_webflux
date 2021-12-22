package com.springboot.springbootwebflux;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.springboot.springbootwebflux.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) { // 1
        return username -> userRepository.findByName(username) // 2
            .map(user -> User.withDefaultPasswordEncoder() // 3
                .username(user.getName())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(new String[0]))
                .build());
    }

    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
            operations.save(
                new com.springboot.springbootwebflux.domain.User("admin",
                    "password", Arrays.asList("ROLE_USER")));
        };
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService2(UserRepository userRepository) {
        return username -> userRepository.findByName(username)
            .map(user -> User.withUsername(user.getName())
                .password(passwordEncoder().encode(user.getPassword()))
                .authorities(user.getRoles().toArray(new String[0]))
                .build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
