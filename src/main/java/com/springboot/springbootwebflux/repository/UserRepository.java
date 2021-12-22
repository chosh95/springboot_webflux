package com.springboot.springbootwebflux.repository;

import org.springframework.data.repository.CrudRepository;

import com.springboot.springbootwebflux.domain.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends CrudRepository<User, String> {

    Mono<User> findByName(String name);
}
