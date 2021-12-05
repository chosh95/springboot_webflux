package com.springboot.springbootwebflux.repository;

import com.springboot.springbootwebflux.domain.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {
}
