package com.example.haunted.engine;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.*;
import com.example.haunted.rules.QuestTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InteractionEngineTest {
    private InteractionEngine engine;
    private Player player;
    private Room room;
    private Quest quest;

    @BeforeEach
    void setUp() {
        engine = new InteractionEngine(new QuestTracker());
        player = new Player("Student", 30, 5, 2, new Inventory(2));
        room = new Room("start", "Hall", "");
        player.setCurrentRoom(room);
        quest = new Quest("Find gradebook", "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Missing", "lost gradebook"})
    void missingPickupFailsWithoutChangingInventory(String name) {
        assertFalse(engine.pickUpItem(player, quest, name).isSuccess());
        assertTrue(player.getInventory().getItems().isEmpty());
        assertFalse(quest.isGradebookRecovered());
    }

    @Test
    void pickupRemovesItemFromRoomAndAddsToInventory() {
        Item item = new Key("Blue Key", "");
        room.addItem(item);
        InteractionResult result = engine.pickUpItem(player, quest, "blue key");
        assertTrue(result.isSuccess());
        assertTrue(player.getInventory().contains("Blue Key"));
        assertTrue(room.findItem("Blue Key").isEmpty());
    }

    @Test
    void pickupGradebookUpdatesQuest() {
        room.addItem(new QuestItem("Lost Gradebook", ""));
        assertTrue(engine.pickUpItem(player, quest, "lost gradebook").isSuccess());
        assertTrue(quest.isGradebookRecovered());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void fullInventoryReturnsItemToRoomAndDoesNotAdvanceQuest() {
        player.getInventory().addItem(new Key("One", ""));
        player.getInventory().addItem(new Key("Two", ""));
        Item item = new QuestItem("Lost Gradebook", "");
        room.addItem(item);
        assertFalse(engine.pickUpItem(player, quest, "Lost Gradebook").isSuccess());
        assertSame(item, room.findItem("Lost Gradebook").orElseThrow());
        assertFalse(player.getInventory().contains("Lost Gradebook"));
        assertFalse(quest.isGradebookRecovered());
    }

    @Test
    void missingItemCannotBeUsed() {
        assertFalse(engine.useItem(player, "Potion").isSuccess());
        assertEquals(30, player.getHealth());
    }

    @Test
    void nonPotionCannotBeUsedOrConsumed() {
        player.getInventory().addItem(new Key("Key", ""));
        assertFalse(engine.useItem(player, "Key").isSuccess());
        assertTrue(player.getInventory().contains("Key"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 25})
    void potionHealsUpToMaximumAndIsConsumed(int damage) {
        player.takeDamage(damage);
        player.getInventory().addItem(new Potion("Health Potion", "", 10));
        assertTrue(engine.useItem(player, "health potion").isSuccess());
        assertEquals(Math.min(30, 30 - damage + 10), player.getHealth());
        assertFalse(player.getInventory().contains("Health Potion"));
    }

    @Test
    void missingEquipmentFails() {
        assertFalse(engine.equipItem(player, "Sword").isSuccess());
        assertNull(player.getEquippedWeapon());
    }

    @Test
    void weaponCanBeEquippedAndStaysInInventory() {
        Weapon weapon = new Weapon("Sword", "", 4);
        player.getInventory().addItem(weapon);
        assertTrue(engine.equipItem(player, "sword").isSuccess());
        assertSame(weapon, player.getEquippedWeapon());
        assertEquals(9, player.getAttackPower());
        assertTrue(player.getInventory().contains("Sword"));
    }

    @Test
    void armorCanBeEquipped() {
        Armor armor = new Armor("Coat", "", 3);
        player.getInventory().addItem(armor);
        assertTrue(engine.equipItem(player, "Coat").isSuccess());
        assertSame(armor, player.getEquippedArmor());
        assertEquals(5, player.getDefensePower());
    }

    @Test
    void keyCannotBeEquipped() {
        player.getInventory().addItem(new Key("Key", ""));
        assertFalse(engine.equipItem(player, "Key").isSuccess());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void unlockingWithoutExitFails(Direction direction) {
        assertFalse(engine.unlockRoom(player, direction).isSuccess());
    }

    @Test
    void alreadyUnlockedRoomReportsSuccessWithoutKey() {
        Room target = targetRoom();
        assertTrue(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertFalse(target.isLocked());
    }

    @Test
    void roomWithoutRequiredKeyCannotBeUnlocked() {
        Room target = targetRoom();
        target.setLocked(true, null);
        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    void missingKeyCannotUnlockRoom() {
        Room target = targetRoom();
        target.setLocked(true, "Blue Key");
        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    void itemWithKeyNameButWrongTypeCannotUnlock() {
        Room target = targetRoom();
        target.setLocked(true, "Blue Key");
        player.getInventory().addItem(new Potion("Blue Key", "", 4));
        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    void correctKeyUnlocksRoomAndIsNotConsumed() {
        Room target = targetRoom();
        target.setLocked(true, "Blue Key");
        player.getInventory().addItem(new Key("blue key", ""));
        assertTrue(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertFalse(target.isLocked());
        assertTrue(player.getInventory().contains("Blue Key"));
    }

    private Room targetRoom() {
        Room target = new Room("next", "Lab", "");
        room.connect(Direction.NORTH, target);
        return target;
    }
}
