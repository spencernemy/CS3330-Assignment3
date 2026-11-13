
package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ItemTypesTest {

    @Test
    void itemNameDescriptionAndToStringAreCorrect() {
        Item item = new Key("Blue Key", "Opens a door");

        assertEquals("Blue Key", item.getName());
        assertEquals("Opens a door", item.getDescription());
        assertEquals("Blue Key", item.toString());
    }

    @Test
    void weaponAndArmorHaveCorrectBonuses() {
        Weapon weapon = new Weapon("Sword", "", 5);
        Armor armor = new Armor("Shield", "", 3);

        assertEquals(5, weapon.getAttackBonus());
        assertEquals(3, armor.getDefenseBonus());
        assertTrue(weapon instanceof Equippable);
        assertTrue(armor instanceof Equippable);
    }

    @Test
    void potionHasCorrectHealingAmount() {
        Potion potion = new Potion("Potion", "", 10);

        assertEquals(10, potion.getHealingAmount());
        assertTrue(potion instanceof Usable);
    }

    @ParameterizedTest
    @CsvSource({
        "-5, 10",
        "0, 10",
        "5, 15",
        "20, 30",
        "50, 30"
    })
    void potionHealingStaysWithinHealthLimits(int healing, int expected) {
        Player player = new Player("Student", 30, 5, 2, new Inventory(5));
        player.takeDamage(20);

        Potion potion = new Potion("Potion", "", healing);
        potion.use(player);

        assertEquals(expected, player.getHealth());
    }

    @Test
    void questItemStoresItsName() {
        QuestItem item = new QuestItem("Lost Gradebook", "");

        assertEquals("Lost Gradebook", item.getName());
    }

    @Test
    void nullItemFieldsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new Key(null, ""));

        assertThrows(NullPointerException.class,
                () -> new Key("Key", null));
    }
}
