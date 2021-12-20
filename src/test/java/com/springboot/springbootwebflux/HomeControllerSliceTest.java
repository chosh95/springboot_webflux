package com.springboot.springbootwebflux;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.springboot.springbootwebflux.controller.HomeController;
import com.springboot.springbootwebflux.domain.Cart;
import com.springboot.springbootwebflux.domain.Item;
import com.springboot.springbootwebflux.service.InventoryService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HomeController.class)
public class HomeControllerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    InventoryService inventoryService;

    @Test
    void homePage() {
        when(inventoryService.getInventory()).thenReturn(Flux.just(
                new Item("id1", "name1", "desc1", 1),
                new Item("id2", "name2", "desc2", 2)
        ));
        when(inventoryService.getCart("My Cart"))
                .thenReturn(Mono.just(new Cart("My Cart")));

        client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(exchangeResult -> {
                    assertThat(exchangeResult.getResponseBody()).contains("action=\"/add/id1\"");
                    assertThat(exchangeResult.getResponseBody()).contains("action=\"/add/id2\"");
                });
    }
}
