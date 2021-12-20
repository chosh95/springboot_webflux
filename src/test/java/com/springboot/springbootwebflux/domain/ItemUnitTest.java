package com.springboot.springbootwebflux.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemUnitTest {

    @Test
    void itemBasicsShouldWork() {
        Item sampleItem = new Item("item1", "TV", "Alf TV tray", 19.99);

        assertThat(sampleItem.getId()).isEqualTo("item1");
        assertEquals(sampleItem.getName(), "TV");
        assertEquals(sampleItem.getDescription(), "Alf TV tray");
        assertEquals(sampleItem.getPrice(), 19.99);

        assertEquals(sampleItem.toString(), "Item{id='item1', name='TV', description='Alf TV tray', price=19.99}");

        Item sampleItem2 = new Item("item1", "TV", "Alf TV tray", 19.99);
        assertThat(sampleItem).isEqualTo(sampleItem2);
    }

}