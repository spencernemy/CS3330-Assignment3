package com.example.haunted.engine;

import com.example.haunted.events.MoveResult;
import com.example.haunted.model.*;
import com.example.haunted.rules.TrapResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MovementEngineTest {
    private MovementEngine engine;
    private Player player;
    private Room start;
    private Room next;

    @BeforeEach
    void setUp() {
        engine = new MovementEngine(new TrapResolver());
        player = new Player("Student", 30, 8, 2, new Inventory(5));
        start = new Room("start", "Hall", "");
        next = new Room("next", "Lab", "");
        player.setCurrentRoom(start);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void missingExitDoesNotMove(Direction direction) {
        MoveResult result = engine.move(player, direction);
        assertFalse(result.isSuccess());
        assertSame(start, player.getCurrentRoom());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
    }

    @Test
    void lockedRoomCannotBeEntered() {
        start.connect(Direction.NORTH, next);
        next.setLocked(true, "Key");
        MoveResult result = engine.move(player, Direction.NORTH);
        assertFalse(result.isSuccess());
        assertSame(start, player.getCurrentRoom());
        assertEquals(30, player.getHealth());
    }

    @Test
    void movingToSafeRoomChangesCurrentRoom() {
        start.connect(Direction.EAST, next);
        MoveResult result = engine.move(player, Direction.EAST);
        assertTrue(result.isSuccess());
        assertSame(next, player.getCurrentRoom());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    void unarmedOrMissingTrapDoesNotTrigger(boolean hasTrap, boolean armed) {
        start.connect(Direction.SOUTH, next);
        if (hasTrap) next.setTrap(new Trap("Steam", TrapType.STEAM, 7, armed, true));
        MoveResult result = engine.move(player, Direction.SOUTH);
        assertTrue(result.isSuccess());
        assertFalse(result.isTrapTriggered());
        assertEquals(30, player.getHealth());
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    void armedTrapDamagesPlayerAndDisarmsOnlyWhenOneTime(boolean oneTime, boolean remainsArmed) {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 7, true, oneTime);
        next.setTrap(trap);
        start.connect(Direction.WEST, next);
        MoveResult result = engine.move(player, Direction.WEST);
        assertTrue(result.isSuccess());
        assertTrue(result.isTrapTriggered());
        assertEquals(7, result.getTrapDamage());
        assertEquals(23, player.getHealth());
        assertEquals(remainsArmed, trap.isArmed());
        assertSame(next, player.getCurrentRoom());
    }

    @Test
    void oneTimeTrapDoesNotTriggerOnSecondVisit() {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 4, true, true);
        next.setTrap(trap);
        start.connect(Direction.EAST, next);
        next.connect(Direction.WEST, start);
        assertTrue(engine.move(player, Direction.EAST).isTrapTriggered());
        engine.move(player, Direction.WEST);
        assertFalse(engine.move(player, Direction.EAST).isTrapTriggered());
        assertEquals(26, player.getHealth());
    }

    @Test
    void recurringTrapTriggersAgain() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 4, true, false);
        next.setTrap(trap);
        start.connect(Direction.EAST, next);
        next.connect(Direction.WEST, start);
        engine.move(player, Direction.EAST);
        engine.move(player, Direction.WEST);
        assertTrue(engine.move(player, Direction.EAST).isTrapTriggered());
        assertEquals(22, player.getHealth());
    }
}
