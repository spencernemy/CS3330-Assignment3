
package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MonsterTest {

    @Test
    void monsterStartsWithCorrectStats() {
        Monster monster = new Monster("Ghost", 20, 8, 3, List.of());

        assertEquals("Ghost", monster.getName());
        assertEquals(20, monster.getHealth());
        assertEquals(20, monster.getMaxHealth());
        assertEquals(8, monster.getAttack());
        assertEquals(3, monster.getDefense());
        assertTrue(monster.isAlive());
    }

    @ParameterizedTest
    @CsvSource({
        "-5, 20",
        "0, 20",
        "1, 19",
        "10, 10",
        "20, 0",
        "50, 0"
    })
    void damageCannotMakeHealthNegative(int damage, int expected) {
        Monster monster = new Monster("Ghost", 20, 5, 2, List.of());
        monster.takeDamage(damage);

        assertEquals(expected, monster.getHealth());
        assertEquals(expected > 0, monster.isAlive());
    }

    @Test
    void zeroHealthMonsterStartsDead() {
        Monster monster = new Monster("Ghost", 0, 5, 2, List.of());

        assertFalse(monster.isAlive());
        assertEquals(0, monster.getHealth());
    }

    @Test
    void damageDoesNotChangeMaximumHealth() {
        Monster monster = new Monster("Ghost", 20, 5, 2, List.of());
        monster.takeDamage(7);

        assertEquals(13, monster.getHealth());
        assertEquals(20, monster.getMaxHealth());
    }

    @Test
    void lootIsCopiedAndCannotBeModifiedDirectly() {
        Item key = new Key("Key", "");
        List<Item> loot = new ArrayList<>();
        loot.add(key);

        Monster monster = new Monster("Ghost", 10, 5, 0, loot);
        loot.clear();

        assertEquals(1, monster.getLoot().size());
        assertSame(key, monster.getLoot().get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> monster.getLoot().clear());
    }
}
