package com.springboot.springbootwebflux.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springboot.springbootwebflux.domain.Item;
import com.springboot.springbootwebflux.repository.ItemRepository;

import reactor.test.StepVerifier;

@SpringBootTest()
@EnableHypermediaSupport(type = HAL) // <1>
@AutoConfigureWebTestClient
public class ApiItemControllerTest {

    @Autowired
    WebTestClient webTestClient; // <2>

    @Autowired
    ItemRepository repository;

    @Autowired
    HypermediaWebTestClientConfigurer webClientConfigurer; // <3>

    @BeforeEach
    void setUp() {
        this.webTestClient = this.webTestClient.mutateWith(webClientConfigurer); // <4>
    }

    @Test
    void noCredentialsFailsAtRoot() {
        this.webTestClient.get().uri("/api") //
            .exchange() //
            .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "ada")
    void credentialsWorksOnRoot() {
        this.webTestClient.get().uri("/api") //
            .exchange() //
            .expectStatus().isOk() //
            .expectBody(String.class) //
            .isEqualTo("{\"_links\":{\"self\":{\"href\":\"/api\"},\"item\":{\"href\":\"/api/items\"}}}");
    }

    // tag::add-inventory-without-role[]
    @Test
    @WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) // <1>
    void addingInventoryWithoutProperRoleFails() {
        this.webTestClient //
            .post().uri("/api/items/add") // <2>
            .contentType(MediaType.APPLICATION_JSON) //
            .bodyValue("{" + //
                "\"name\": \"iPhone X\", " + //
                "\"description\": \"upgrade\", " + //
                "\"price\": 999.99" + //
                "}") //
            .exchange() //
            .expectStatus().isForbidden(); // <3>
    }
    // end::add-inventory-without-role[]

    // tag::add-inventory-with-role[]
    @Test
    @WithMockUser(username = "bob", roles = { "INVENTORY" }) // <1>
    void addingInventoryWithProperRoleSucceeds() {
        this.webTestClient //
            .post().uri("/api/items/add") // <2>
            .contentType(MediaType.APPLICATION_JSON) //
            .bodyValue("{" + //
                "\"name\": \"iPhone X\", " + //
                "\"description\": \"upgrade\", " + //
                "\"price\": 999.99" + //
                "}") //
            .exchange() //
            .expectStatus().isCreated(); // <3>

        this.repository.findByName("iPhone X") // <4>
            .as(StepVerifier::create) //
            .expectNextMatches(item -> { //
                assertThat(item.getDescription()).isEqualTo("upgrade");
                assertThat(item.getPrice()).isEqualTo(999.99);
                return true; //
            }) //
            .verifyComplete(); //
    }
    // end::add-inventory-with-role[]

    @Test
    @WithMockUser(username = "carol", roles = { "SOME_OTHER_ROLE" })
    void deletingInventoryWithoutProperRoleFails() {
        this.webTestClient.delete().uri("/api/items/delete/some-item") //
            .exchange() //
            .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "dan", roles = { "INVENTORY" })
    void deletingInventoryWithProperRoleSucceeds() {
        String id = this.repository.findByName("Alf alarm clock") //
            .map(Item::getId) //
            .block();

        this.webTestClient //
            .delete().uri("/api/items/delete/" + id) //
            .exchange() //
            .expectStatus().isNoContent();

        this.repository.findByName("Alf alarm clock") //
            .as(StepVerifier::create) //
            .expectNextCount(0) //
            .verifyComplete();
    }

    @Test
    @WithMockUser(username = "alice")
    void navigateToItemWithoutInventoryAuthority() {
        RepresentationModel<?> root = this.webTestClient.get().uri("/api") //
            .exchange() //
            .expectBody(RepresentationModel.class) //
            .returnResult().getResponseBody();

        CollectionModel<EntityModel<Item>> items = this.webTestClient.get() //
            .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri()) //
            .exchange() //
            .expectBody(new TypeReferences.CollectionModelType<EntityModel<Item>>() {}) //
            .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(1);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();

        EntityModel<Item> first = items.getContent().iterator().next();

        EntityModel<Item> item = this.webTestClient.get() //
            .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
            .exchange() //
            .expectBody(new TypeReferences.EntityModelType<Item>() {}) //
            .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(2);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
    }

    // tag::navigate[]
    @Test
    @WithMockUser(username = "alice", roles = { "INVENTORY" })
    void navigateToItemWithInventoryAuthority() {

        // Navigate to the root URI of the API.
        RepresentationModel<?> root = this.webTestClient.get().uri("/api") //
            .exchange() //
            .expectBody(RepresentationModel.class) //
            .returnResult().getResponseBody();

        // Drill down to the Item aggregate root.
        CollectionModel<EntityModel<Item>> items = this.webTestClient.get() //
            .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri()) //
            .exchange() //
            .expectBody(new TypeReferences.CollectionModelType<EntityModel<Item>>() {}) //
            .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(2);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(items.hasLink("add")).isTrue();

        // Find the first Item...
        EntityModel<Item> first = items.getContent().iterator().next();

        // ...and extract it's single-item entry.
        EntityModel<Item> item = this.webTestClient.get() //
            .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
            .exchange() //
            .expectBody(new TypeReferences.EntityModelType<Item>() {}) //
            .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(3);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
        assertThat(item.hasLink("delete")).isTrue();
    }
    // end::navigate[]

}