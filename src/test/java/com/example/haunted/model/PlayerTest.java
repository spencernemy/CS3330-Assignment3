
package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Student", 30, 10, 3, new Inventory(5));
    }

    @Test
    void playerStartsWithCorrectValues() {
        assertEquals("Student", player.getName());
        assertEquals(30, player.getHealth());
        assertEquals(30, player.getMaxHealth());
        assertEquals(10, player.getBaseAttack());
        assertEquals(3, player.getBaseDefense());
        assertTrue(player.isAlive());
        assertNull(player.getCurrentRoom());
    }

    @ParameterizedTest
    @CsvSource({
        "-5, 30",
        "0, 30",
        "1, 29",
        "15, 15",
        "30, 0",
        "50, 0"
    })
    void damageCannotMakeHealthNegative(int damage, int expected) {
        player.takeDamage(damage);

        assertEquals(expected, player.getHealth());
        assertEquals(expected > 0, player.isAlive());
    }

    @ParameterizedTest
    @CsvSource({
        "-5, 10",
        "0, 10",
        "5, 15",
        "20, 30",
        "50, 30"
    })
    void healingCannotExceedMaxHealth(int healing, int expected) {
        player.takeDamage(20);
        player.heal(healing);

        assertEquals(expected, player.getHealth());
    }

    @Test
    void healingDoesNotReviveBeyondMaxHealth() {
        player.takeDamage(100);
        player.heal(100);

        assertEquals(30, player.getHealth());
        assertTrue(player.isAlive());
    }

    @Test
    void equipmentIncreasesAttackAndDefense() {
        Weapon weapon = new Weapon("Sword", "", 5);
        Armor armor = new Armor("Shield", "", 4);

        player.equipWeapon(weapon);
        player.equipArmor(armor);

        assertSame(weapon, player.getEquippedWeapon());
        assertSame(armor, player.getEquippedArmor());
        assertEquals(15, player.getAttackPower());
        assertEquals(7, player.getDefensePower());
    }

    @Test
    void newEquipmentReplacesOldEquipment() {
        player.equipWeapon(new Weapon("Old Sword", "", 5));
        player.equipWeapon(new Weapon("New Sword", "", 2));

        player.equipArmor(new Armor("Old Armor", "", 4));
        player.equipArmor(new Armor("New Armor", "", 1));

        assertEquals(12, player.getAttackPower());
        assertEquals(4, player.getDefensePower());
    }

    @Test
    void settingCurrentRoomChangesLocation() {
        Room room = new Room("1", "Hall", "");
        player.setCurrentRoom(room);

        assertSame(room, player.getCurrentRoom());
    }

    @Test
    void nullRoomOrEquipmentIsRejected() {
        assertThrows(NullPointerException.class,
                () -> player.setCurrentRoom(null));
        assertThrows(NullPointerException.class,
                () -> player.equipWeapon(null));
        assertThrows(NullPointerException.class,
                () -> player.equipArmor(null));
    }
}
