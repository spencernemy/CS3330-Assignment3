
package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TrapTest {

    @Test
    void trapStoresCorrectValues() {
        Trap trap = new Trap("Shock", TrapType.ELECTRIC, 8, true, true);

        assertEquals("Shock", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertTrue(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void disarmMakesTrapInactive(boolean initiallyArmed) {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5,
                initiallyArmed, false);

        trap.disarm();

        assertFalse(trap.isArmed());
        assertFalse(trap.isOneTimeTrigger());
    }

    @Test
    void trapCanStartDisarmed() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5, false, true);

        assertFalse(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
    }

    @Test
    void nullTrapTypeIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new Trap("Trap", null, 5, true, true));
    }
}
