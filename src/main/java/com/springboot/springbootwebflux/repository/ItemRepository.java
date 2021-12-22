package com.springboot.springbootwebflux.repository;

import com.springboot.springbootwebflux.domain.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {

    Mono<Item> findByName(String name);
}
