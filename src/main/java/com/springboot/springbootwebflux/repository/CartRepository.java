package com.springboot.springbootwebflux.repository;

import com.springboot.springbootwebflux.domain.Cart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<Cart, String> {
}
