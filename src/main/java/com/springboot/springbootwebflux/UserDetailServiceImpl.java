package com.springboot.springbootwebflux;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.springboot.springbootwebflux.repository.UserRepository;

import reactor.core.publisher.Mono;

// @Component
public class UserDetailServiceImpl implements ReactiveUserDetailsService {

    private UserRepository userRepository;

    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByName(username)
            .map(user -> User.withUsername(user.getName())
                .password(SecurityConfig.encoder.encode(user.getPassword()))
                .authorities(user.getRoles().toArray(new String[0]))
                .build());
    }
}
