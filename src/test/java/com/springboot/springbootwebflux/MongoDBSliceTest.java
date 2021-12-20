package com.springboot.springbootwebflux;

import com.springboot.springbootwebflux.domain.Item;
import com.springboot.springbootwebflux.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class MongoDBSliceTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void itemRepositorySavesItems() {
        Item sampleItem = new Item("name", "description", 19.99);

        itemRepository.save(sampleItem)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("name");

                    return true;
                })
                .verifyComplete();
    }
}
