package com.springboot.springbootwebflux.controller;

import com.springboot.springbootwebflux.domain.Dish;
import com.springboot.springbootwebflux.service.KitchenService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class ServerController {

    private final KitchenService kitchen;

    public ServerController(KitchenService kitchen) {
        this.kitchen = kitchen;
    }

    @GetMapping(value = "/server", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Dish> ServeDishes() {
        return this.kitchen.getDishes();
    }

    @GetMapping(value = "/server-dishes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Dish> deliverDishes() {
        return this.kitchen.getDishes()
            .map(dish -> Dish.deliver(dish));
    }
}
