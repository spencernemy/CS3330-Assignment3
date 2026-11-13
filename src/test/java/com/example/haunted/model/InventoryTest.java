
package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(2);
    }

    @Test
    void inventoryStartsEmpty() {
        assertEquals(2, inventory.getCapacity());
        assertTrue(inventory.getItems().isEmpty());
        assertFalse(inventory.isFull());
    }

    @Test
    void addingItemsUpToCapacityWorks() {
        assertTrue(inventory.addItem(new Key("Key", "")));
        assertFalse(inventory.isFull());

        assertTrue(inventory.addItem(new Potion("Potion", "", 5)));
        assertTrue(inventory.isFull());
        assertEquals(2, inventory.getItems().size());
    }

    @Test
    void addingBeyondCapacityFails() {
        inventory.addItem(new Key("One", ""));
        inventory.addItem(new Key("Two", ""));

        assertFalse(inventory.addItem(new Key("Three", "")));
        assertEquals(2, inventory.getItems().size());
        assertFalse(inventory.contains("Three"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Blue Key", "blue key", "BLUE KEY"})
    void itemSearchIgnoresCase(String name) {
        Item item = new Key("Blue Key", "");
        inventory.addItem(item);

        assertTrue(inventory.contains(name));
        assertSame(item, inventory.findItem(name).orElseThrow());
    }

    @Test
    void removingItemFreesCapacity() {
        Item item = new Key("Key", "");
        inventory.addItem(item);
        inventory.addItem(new Potion("Potion", "", 5));

        assertSame(item, inventory.removeItem("key"));
        assertFalse(inventory.isFull());
        assertFalse(inventory.contains("Key"));
        assertTrue(inventory.addItem(new Key("New Key", "")));
    }

    @Test
    void removingMissingItemReturnsNull() {
        assertNull(inventory.removeItem("Missing"));
        assertTrue(inventory.findItem("Missing").isEmpty());
    }

    @Test
    void zeroCapacityInventoryCannotHoldItems() {
        Inventory empty = new Inventory(0);

        assertTrue(empty.isFull());
        assertFalse(empty.addItem(new Key("Key", "")));
        assertTrue(empty.getItems().isEmpty());
    }

    @Test
    void returnedItemListCannotBeModified() {
        inventory.addItem(new Key("Key", ""));

        assertThrows(UnsupportedOperationException.class,
                () -> inventory.getItems().clear());
        assertEquals(1, inventory.getItems().size());
    }
}
