package com.springboot.springbootwebflux;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.springboot.springbootwebflux.repository.UserRepository;

@Configuration
public class SecurityConfig {

    public static final String USER = "USER"; // 일반 사용자
    public static final String INVENTORY = "INVENTORY"; // 개발자

    @Bean
    SecurityWebFilterChain customSecurityPolicy(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.POST, "/").hasRole(INVENTORY)
                .pathMatchers(HttpMethod.DELETE, "/**").hasRole(INVENTORY)
            .anyExchange().authenticated()
            .and()
            .httpBasic()
            .and()
            .formLogin())
            .csrf().disable()
            .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByName(username)
            .map(user -> User.withUsername(user.getName())
                .password(passwordEncoder().encode(user.getPassword()))
                .authorities(user.getRoles().toArray(new String[0]))
                .build());
    }

    static String role(String auth) {
        return "ROLE_" + auth;
    }

    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
            operations.save(
                new com.springboot.springbootwebflux.domain.User(
                    "user", "password", Arrays.asList(role(USER))));
            operations.save(
                new com.springboot.springbootwebflux.domain.User(
                    "manager", "password", Arrays.asList(role(USER), role(INVENTORY))));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) { // 1
    //     return username -> userRepository.findByName(username) // 2
    //         .map(user -> User.withDefaultPasswordEncoder() // 3
    //             .username(user.getName())
    //             .password(user.getPassword())
    //             .authorities(user.getRoles().toArray(new String[0]))
    //             .build());
    // }
}
