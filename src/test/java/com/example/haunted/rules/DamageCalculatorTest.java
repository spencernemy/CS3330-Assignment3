
package com.example.haunted.rules;

import com.example.haunted.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DamageCalculatorTest {
    private DamageCalculator calculator;
    private Player player;

    @BeforeEach
    void setUp() {
        calculator = new DamageCalculator();
        player = new Player("Student", 30, 10, 2, new Inventory(5));
    }

    @ParameterizedTest
    @CsvSource({
        "0, 10",
        "5, 5",
        "9, 1",
        "10, 1",
        "15, 1"
    })
    void playerDamageRespectsMonsterDefense(int defense, int expected) {
        Monster monster = monster(5, defense);
        assertEquals(expected, calculator.calculatePlayerDamage(player, monster));
    }

    @Test
    void equippedWeaponIncreasesPlayerDamage() {
        player.equipWeapon(new Weapon("Sword", "", 5));
        Monster monster = monster(5, 3);

        assertEquals(12, calculator.calculatePlayerDamage(player, monster));
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "2, 1",
        "3, 1",
        "5, 3",
        "10, 8"
    })
    void monsterDamageRespectsPlayerDefense(int attack, int expected) {
        Monster monster = monster(attack, 0);
        assertEquals(expected, calculator.calculateMonsterDamage(monster, player));
    }

    @Test
    void equippedArmorReducesMonsterDamage() {
        player.equipArmor(new Armor("Shield", "", 4));
        Monster monster = monster(10, 0);

        assertEquals(4, calculator.calculateMonsterDamage(monster, player));
    }

    @Test
    void strongArmorCannotReduceDamageBelowOne() {
        player.equipArmor(new Armor("Heavy Armor", "", 20));
        Monster monster = monster(5, 0);

        assertEquals(1, calculator.calculateMonsterDamage(monster, player));
    }

    @ParameterizedTest
    @CsvSource({
        "20, 3",
        "11, 3",
        "10, 9",
        "9, 9",
        "1, 9",
        "0, 9"
    })
    void bossDamageChangesAtHalfHealth(int health, int expected) {
        BossMonster boss = new BossMonster("Boss", 20, 5, 0, List.of(), 6);
        boss.takeDamage(20 - health);

        assertEquals(expected, calculator.calculateMonsterDamage(boss, player));
    }

    @Test
    void bossWithoutBonusStillUsesNormalAttack() {
        BossMonster boss = new BossMonster("Boss", 20, 8, 0, List.of(), 0);
        boss.takeDamage(10);

        assertEquals(6, calculator.calculateMonsterDamage(boss, player));
    }

    private Monster monster(int attack, int defense) {
        return new Monster("Ghost", 20, attack, defense, List.of());
    }
}
