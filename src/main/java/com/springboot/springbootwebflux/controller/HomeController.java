package com.springboot.springbootwebflux.controller;

import com.springboot.springbootwebflux.domain.Cart;
import com.springboot.springbootwebflux.domain.Item;
import com.springboot.springbootwebflux.service.InventoryService;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    private final InventoryService inventoryService;

    public HomeController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    private static String cartName(Authentication auth) {
        return auth.getName() + "'s Cart";
    }

    @GetMapping
    Mono<Rendering> home(Authentication auth) {
        return Mono.just(Rendering.view("home.html")
            .modelAttribute("items", this.inventoryService.getInventory())
            .modelAttribute("cart", this.inventoryService.getCart(cartName(auth))
                .defaultIfEmpty(new Cart(cartName(auth))))
            .modelAttribute("auth", auth)
            .build());
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(Authentication auth, @PathVariable String id) {
        return this.inventoryService.addItemToCart(cartName(auth), id)
            .thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(Authentication auth, @PathVariable String id) {
        return this.inventoryService.removeOneFromCart(cartName(auth), id)
            .thenReturn("redirect:/");
    }

    @PostMapping
    @ResponseBody
    Mono<Item> createItem(@RequestBody Item newItem) {
        return this.inventoryService.saveItem(newItem);
    }

    @PreAuthorize("hasRole('ROLE_INVENTORY')")
    @DeleteMapping("/delete/{id}")
    Mono<String> deleteItem(@PathVariable String id) {
        return this.inventoryService.deleteItem(id)
            .thenReturn("redirect:/");
    }
}
