
package com.example.haunted.rules;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class TrapResolverTest {
    private TrapResolver resolver;
    private Player player;

    @BeforeEach
    void setUp() {
        resolver = new TrapResolver();
        player = new Player("Student", 30, 10, 2, new Inventory(5));
    }

    @Test
    void nullTrapDoesNotTrigger() {
        InteractionResult result = resolver.resolveTrap(player, null);

        assertFalse(result.isSuccess());
        assertEquals(30, player.getHealth());
    }

    @Test
    void disarmedTrapDoesNotDamagePlayer() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 10, false, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertFalse(result.isSuccess());
        assertEquals(30, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 30",
        "1, 29",
        "10, 20",
        "29, 1",
        "30, 0",
        "50, 0"
    })
    void trapDamageRespectsPlayerHealth(int damage, int expectedHealth) {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, damage, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(expectedHealth, player.getHealth());
    }

    @Test
    void negativeDamageDoesNotHealOrHurtPlayer() {
        player.takeDamage(10);
        Trap trap = new Trap("Bad Trap", TrapType.STEAM, -5, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(20, player.getHealth());
    }

    @Test
    void oneTimeTrapDisarmsAfterTriggering() {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 5, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(25, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @Test
    void oneTimeTrapCannotTriggerTwice() {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 5, true, true);

        assertTrue(resolver.resolveTrap(player, trap).isSuccess());
        assertFalse(resolver.resolveTrap(player, trap).isSuccess());

        assertEquals(25, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @Test
    void repeatableTrapStaysArmed() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5, true, false);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(25, player.getHealth());
        assertTrue(trap.isArmed());
    }

    @Test
    void repeatableTrapDamagesPlayerMultipleTimes() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5, true, false);

        assertTrue(resolver.resolveTrap(player, trap).isSuccess());
        assertTrue(resolver.resolveTrap(player, trap).isSuccess());
        assertTrue(resolver.resolveTrap(player, trap).isSuccess());

        assertEquals(15, player.getHealth());
        assertTrue(trap.isArmed());
    }

    @Test
    void trapCanTriggerWhenPlayerIsAlreadyDead() {
        player.takeDamage(30);
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 10, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(0, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @Test
    void disarmedTrapRemainsInactiveEvenIfRepeatable() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5, false, false);

        assertFalse(resolver.resolveTrap(player, trap).isSuccess());
        assertFalse(resolver.resolveTrap(player, trap).isSuccess());

        assertEquals(30, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @ParameterizedTest
    @EnumSource(TrapType.class)
    void allTrapTypesDealDamage(TrapType type) {
        Trap trap = new Trap("Trap", type, 4, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(26, player.getHealth());
    }

    @Test
    void trapResultContainsCorrectMessage() {
        Trap trap = new Trap("Steam Vent", TrapType.STEAM, 7, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Steam Vent"));
        assertTrue(result.getMessage().contains("7"));
    }
}
