package com.example.haunted.engine;

import com.example.haunted.model.*;
import com.example.haunted.rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {
    private GameEngine game;
    private Player player;
    private Quest quest;
    private Room start;

    @BeforeEach
    void setUp() {
        player = new Player("Student", 30, 10, 2, new Inventory(10));
        start = new Room("start", "Hall", "");
        player.setCurrentRoom(start);
        quest = new Quest("Find gradebook", "");
        QuestTracker tracker = new QuestTracker();
        game = new GameEngine(player, quest, new MovementEngine(new TrapResolver()),
                new CombatEngine(new DamageCalculator(), tracker), new InteractionEngine(tracker));
    }

    @Test
    void gettersReturnTheSameObjects() {
        assertSame(player, game.getPlayer());
        assertSame(quest, game.getQuest());
        assertSame(start, game.getCurrentRoom());
    }

    @Test
    void moveDelegatesToMovementEngine() {
        Room next = new Room("next", "Lab", "");
        start.connect(Direction.EAST, next);
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertSame(next, game.getCurrentRoom());
        assertFalse(game.move(Direction.NORTH).isSuccess());
    }

    @Test
    void pickingUpAndUsingPotionDelegatesToInteractionEngine() {
        start.addItem(new Potion("Potion", "", 10));
        player.takeDamage(15);
        assertTrue(game.pickUpItem("Potion").isSuccess());
        assertTrue(game.useItem("Potion").isSuccess());
        assertEquals(25, player.getHealth());
        assertFalse(player.getInventory().contains("Potion"));
    }

    @Test
    void equipAndUnlockDelegateToInteractionEngine() {
        start.addItem(new Weapon("Sword", "", 3));
        assertTrue(game.pickUpItem("Sword").isSuccess());
        assertTrue(game.equipItem("Sword").isSuccess());
        assertEquals(13, player.getAttackPower());
        Room locked = new Room("lab", "Lab", "");
        locked.setLocked(true, "Lab Key");
        start.connect(Direction.NORTH, locked);
        assertFalse(game.unlockRoom(Direction.NORTH).isSuccess());
        start.addItem(new Key("Lab Key", ""));
        assertTrue(game.pickUpItem("Lab Key").isSuccess());
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertFalse(locked.isLocked());
        assertTrue(game.move(Direction.NORTH).isSuccess());
    }

    @Test
    void attacksMonsterByCaseInsensitiveName() {
        Monster ghost = new Monster("Ghost", 5, 3, 0, List.of());
        start.addMonster(ghost);
        assertFalse(game.attack("Missing").isSuccess());
        assertTrue(game.attack("ghost").isMonsterDefeated());
        assertEquals(0, ghost.getHealth());
        assertFalse(game.attack("Ghost").isSuccess());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void questRequiresBothObjectivesForVictory(boolean recoveredFirst) {
        if (recoveredFirst) quest.markGradebookRecovered();
        else quest.markPhantomDefeated();
        assertFalse(game.isGameWon());
        assertFalse(game.isGameOver());
        if (recoveredFirst) quest.markPhantomDefeated();
        else quest.markGradebookRecovered();
        assertTrue(game.isGameWon());
        assertFalse(game.isGameOver());
    }

    @Test
    void completingQuestWhileDeadDoesNotCountAsWin() {
        quest.markGradebookRecovered();
        quest.markPhantomDefeated();
        player.takeDamage(30);
        assertTrue(game.isGameOver());
        assertFalse(game.isGameWon());
    }

    @Test
    void deadPlayerWithIncompleteQuestIsGameOver() {
        player.takeDamage(30);
        assertTrue(game.isGameOver());
        assertFalse(game.isGameWon());
    }

    @Test
    void winningThroughActualItemAndBossActions() {
        start.addItem(new QuestItem("Lost Gradebook", ""));
        start.addMonster(new Monster("Final Exam Phantom", 5, 2, 0, List.of()));
        assertTrue(game.pickUpItem("Lost Gradebook").isSuccess());
        assertFalse(game.isGameWon());
        assertTrue(game.attack("Final Exam Phantom").isMonsterDefeated());
        assertTrue(game.isGameWon());
    }
}
