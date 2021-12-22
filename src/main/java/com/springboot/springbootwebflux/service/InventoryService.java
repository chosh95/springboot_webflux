package com.springboot.springbootwebflux.service;

import com.springboot.springbootwebflux.domain.Cart;
import com.springboot.springbootwebflux.domain.CartItem;
import com.springboot.springbootwebflux.domain.Item;
import com.springboot.springbootwebflux.repository.CartRepository;
import com.springboot.springbootwebflux.repository.ItemRepository;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class InventoryService {

    private ItemRepository itemRepository;

    private CartRepository cartRepository;

    InventoryService(ItemRepository repository,
        CartRepository cartRepository) {
        this.itemRepository = repository;
        this.cartRepository = cartRepository;
    }

    public Mono<Cart> getCart(String cartId) {
        return this.cartRepository.findById(cartId);
    }

    public Flux<Item> getInventory() {
        return this.itemRepository.findAll();
    }

    public Mono<Item> saveItem(Item newItem) {
        return this.itemRepository.save(newItem);
    }

    public Mono<Void> deleteItem(String id) {
        return this.itemRepository.deleteById(id);
    }

    public Mono<Cart> addItemToCart(String cartId, String itemId) {
        return this.cartRepository.findById(cartId)
            .defaultIfEmpty(new Cart(cartId)) //
            .flatMap(cart -> cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .findAny() //
                .map(cartItem -> {
                    cartItem.increment();
                    return Mono.just(cart);
                }) //
                .orElseGet(() -> this.itemRepository.findById(itemId) //
                    .map(item -> new CartItem(item)) //
                    .map(cartItem -> {
                        cart.getCartItems().add(cartItem);
                        return cart;
                    })))
            .flatMap(cart -> this.cartRepository.save(cart));
    }

    public Mono<Cart> removeOneFromCart(String cartId, String itemId) {
        return this.cartRepository.findById(cartId)
            .defaultIfEmpty(new Cart(cartId))
            .flatMap(cart -> cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .findAny()
                .map(cartItem -> {
                    cartItem.decrement();
                    return Mono.just(cart);
                }) //
                .orElse(Mono.empty()))
            .map(cart -> new Cart(cart.getId(), cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getQuantity() > 0)
                .collect(Collectors.toList())))
            .flatMap(cart -> this.cartRepository.save(cart));
    }
}
