
package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoomTest {
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("hall", "Hallway", "A dark hallway");
    }

    @Test
    void roomStartsWithCorrectValues() {
        assertEquals("hall", room.getId());
        assertEquals("Hallway", room.getName());
        assertEquals("A dark hallway", room.getDescription());
        assertTrue(room.getExits().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertTrue(room.getMonsters().isEmpty());
        assertFalse(room.isLocked());
        assertNull(room.getTrap());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void roomsCanConnectInEveryDirection(Direction direction) {
        Room next = new Room("next", "Next", "");
        room.connect(direction, next);

        assertSame(next, room.getExit(direction));
        assertEquals(1, room.getExits().size());
    }

    @Test
    void missingExitReturnsNull() {
        assertNull(room.getExit(Direction.NORTH));
    }

    @Test
    void connectingRoomDoesNotAutomaticallyConnectBack() {
        Room next = new Room("next", "Next", "");
        room.connect(Direction.NORTH, next);

        assertNull(next.getExit(Direction.SOUTH));
    }

    @Test
    void itemsCanBeAddedFoundAndRemoved() {
        Item key = new Key("Blue Key", "");
        room.addItem(key);

        assertSame(key, room.findItem("blue key").orElseThrow());
        assertSame(key, room.removeItemByName("BLUE KEY"));
        assertTrue(room.findItem("Blue Key").isEmpty());
    }

    @Test
    void removingMissingItemReturnsNull() {
        assertNull(room.removeItemByName("Missing"));
        assertTrue(room.findItem("Missing").isEmpty());
    }

    @Test
    void monstersCanBeFoundIgnoringCase() {
        Monster ghost = new Monster("Ghost", 10, 5, 2, List.of());
        room.addMonster(ghost);

        assertSame(ghost, room.findMonster("ghost").orElseThrow());
        assertTrue(room.findMonster("Missing").isEmpty());
    }

    @Test
    void livingMonsterDetectionUpdatesAfterDeath() {
        Monster ghost = new Monster("Ghost", 10, 5, 2, List.of());
        room.addMonster(ghost);

        assertTrue(room.hasLivingMonsters());

        ghost.takeDamage(10);

        assertFalse(room.hasLivingMonsters());
    }

    @Test
    void oneLivingMonsterIsEnough() {
        Monster dead = new Monster("Dead", 5, 1, 0, List.of());
        dead.takeDamage(5);
        Monster living = new Monster("Living", 5, 1, 0, List.of());

        room.addMonster(dead);
        room.addMonster(living);

        assertTrue(room.hasLivingMonsters());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Blue Key", "blue key", "BLUE KEY"})
    void correctKeyUnlocksRoomIgnoringCase(String keyName) {
        room.setLocked(true, "Blue Key");

        assertTrue(room.unlock(keyName));
        assertFalse(room.isLocked());
    }

    @Test
    void wrongKeyDoesNotUnlockRoom() {
        room.setLocked(true, "Blue Key");

        assertFalse(room.unlock("Red Key"));
        assertTrue(room.isLocked());
        assertEquals("Blue Key", room.getRequiredKeyName());
    }

    @Test
    void lockedRoomWithoutRequiredKeyCannotBeUnlocked() {
        room.setLocked(true, null);

        assertFalse(room.unlock("Any Key"));
        assertTrue(room.isLocked());
    }

    @Test
    void unlockedRoomReturnsTrueWithoutKey() {
        assertTrue(room.unlock(null));
        assertFalse(room.isLocked());
    }

    @Test
    void trapCanBeAssignedToRoom() {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 5, true, true);
        room.setTrap(trap);

        assertSame(trap, room.getTrap());
    }

    @Test
    void roomCollectionsCannotBeModifiedDirectly() {
        room.addItem(new Key("Key", ""));
        room.addMonster(new Monster("Ghost", 5, 2, 0, List.of()));

        assertThrows(UnsupportedOperationException.class,
                () -> room.getItems().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> room.getMonsters().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> room.getExits().put(Direction.NORTH, room));
    }

    @Test
    void nullItemsAndMonstersAreRejected() {
        assertThrows(NullPointerException.class,
                () -> room.addItem(null));
        assertThrows(NullPointerException.class,
                () -> room.addMonster(null));
    }
}
