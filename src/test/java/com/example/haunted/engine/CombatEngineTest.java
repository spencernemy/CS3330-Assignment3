package com.example.haunted.engine;

import com.example.haunted.events.CombatResult;
import com.example.haunted.model.*;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CombatEngineTest {
    private CombatEngine engine;
    private Player player;
    private Room room;
    private Quest quest;

    @BeforeEach
    void setUp() {
        engine = new CombatEngine(new DamageCalculator(), new QuestTracker());
        player = new Player("Student", 30, 10, 2, new Inventory(5));
        room = new Room("start", "Hall", "");
        player.setCurrentRoom(room);
        quest = new Quest("Find gradebook", "");
    }

    @Test
    void missingMonsterFailsWithoutDamage() {
        CombatResult result = engine.attack(player, quest, null);
        assertFalse(result.isSuccess());
        assertFalse(result.isMonsterDefeated());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertEquals(30, player.getHealth());
    }

    @Test
    void defeatedPlayerCannotAttack() {
        Monster monster = monster("Ghost", 20, 5, 0);
        player.takeDamage(50);
        CombatResult result = engine.attack(player, quest, monster);
        assertFalse(result.isSuccess());
        assertEquals(20, monster.getHealth());
        assertEquals(0, player.getHealth());
    }

    @Test
    void defeatedMonsterCannotBeAttackedAgain() {
        Monster monster = monster("Ghost", 5, 5, 0);
        monster.takeDamage(5);
        CombatResult result = engine.attack(player, quest, monster);
        assertFalse(result.isSuccess());
        assertTrue(result.isMonsterDefeated());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(30, player.getHealth());
    }

    @ParameterizedTest
    @CsvSource({"20, 5, 2, 8, 3", "20, 1, 20, 1, 1", "20, 9, 0, 10, 7"})
    void livingMonsterCounterattacks(int monsterHealth, int attack, int defense,
                                     int expectedHit, int expectedCounter) {
        Monster monster = monster("Ghost", monsterHealth, attack, defense);
        CombatResult result = engine.attack(player, quest, monster);
        assertTrue(result.isSuccess());
        assertFalse(result.isMonsterDefeated());
        assertEquals(expectedHit, result.getDamageToMonster());
        assertEquals(expectedCounter, result.getDamageToPlayer());
        assertEquals(monsterHealth - expectedHit, monster.getHealth());
        assertEquals(30 - expectedCounter, player.getHealth());
        assertTrue(result.getDroppedItems().isEmpty());
    }

    @Test
    void killingMonsterDropsLootWithoutCounterattack() {
        Item loot = new Key("Copper Key", "");
        Monster monster = new Monster("Ghost", 6, 20, 0, List.of(loot));
        CombatResult result = engine.attack(player, quest, monster);
        assertTrue(result.isSuccess());
        assertTrue(result.isMonsterDefeated());
        assertEquals(10, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertEquals(30, player.getHealth());
        assertEquals(0, monster.getHealth());
        assertEquals(List.of(loot), result.getDroppedItems());
        assertSame(loot, room.findItem("Copper Key").orElseThrow());
    }

    @Test
    void defeatingRegularMonsterDoesNotCompleteQuest() {
        engine.attack(player, quest, monster("Ghost", 1, 2, 0));
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void defeatingFinalExamPhantomUpdatesQuest() {
        Monster boss = monster("Final Exam Phantom", 5, 2, 0);
        CombatResult result = engine.attack(player, quest, boss);
        assertTrue(result.isMonsterDefeated());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void bossGetsEnragedOnLowHealthDuringCounterattack() {
        BossMonster boss = new BossMonster("Final Exam Phantom", 20, 5, 0, List.of(), 6);
        boss.takeDamage(5); // 15 HP: after player's hit, 5 HP, so enraged
        CombatResult result = engine.attack(player, quest, boss);
        assertFalse(result.isMonsterDefeated());
        assertEquals(9, result.getDamageToPlayer()); // (5 + 6) - player defense 2
        assertEquals(21, player.getHealth());
    }

    @Test
    void playerCanBeDefeatedByCounterattack() {
        player.takeDamage(28);
        CombatResult result = engine.attack(player, quest, monster("Ghost", 30, 20, 0));
        assertTrue(result.isSuccess());
        assertEquals(0, player.getHealth());
        assertEquals(18, result.getDamageToPlayer());
    }

    private Monster monster(String name, int health, int attack, int defense) {
        return new Monster(name, health, attack, defense, List.of());
    }
}
