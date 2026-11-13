
package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BossMonsterTest {

    @ParameterizedTest
    @CsvSource({
        "20, 10",
        "11, 10",
        "10, 16",
        "9, 16",
        "1, 16",
        "0, 16"
    })
    void bossEnragesAtHalfHealth(int health, int expectedAttack) {
        BossMonster boss = new BossMonster(
                "Boss", 20, 10, 2, List.of(), 6);

        boss.takeDamage(20 - health);

        assertEquals(expectedAttack, boss.getCurrentAttack());
    }

    @Test
    void oddMaximumHealthUsesIntegerDivision() {
        BossMonster boss = new BossMonster(
                "Boss", 21, 10, 2, List.of(), 5);

        boss.takeDamage(10);
        assertEquals(10, boss.getCurrentAttack());

        boss.takeDamage(1);
        assertEquals(15, boss.getCurrentAttack());
    }
}
