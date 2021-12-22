package com.springboot.springbootwebflux;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springboot.springbootwebflux.repository.ItemRepository;

import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureWebTestClient
public class SecurityTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository itemRepository;

    @Test
    @WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" })
    void notAuthorizedFailTest() {
        this.webTestClient.post().uri("/")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "bob", roles = { "INVENTORY" })
    void AuthorizedSuccessTest() {
        this.webTestClient.post().uri("/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{" +
                "\"name\": \"iPhone 11\", " +
                "\"description\": \"upgrade\", " +
                "\"price\": 999.99" +
                "}")
            .exchange()
            .expectStatus().isOk();

        this.itemRepository.findByName("iPhone 11")
            .as(StepVerifier::create)
            .expectNextMatches(item -> {
                assertThat(item.getDescription()).isEqualTo("upgrade");
                assertThat(item.getPrice()).isEqualTo(999.99);
                return true;
            })
            .verifyComplete();
    }

}
