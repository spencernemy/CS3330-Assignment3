
package com.example.haunted.config;

import com.example.haunted.engine.GameEngine;
import com.example.haunted.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DungeonFactoryTest {
    private GameEngine game;
    private Player player;
    private Room stairwell;
    private Room lectureHall;
    private Room labStorage;
    private Room examArchive;
    private Room deanVault;
    private Room finalChamber;

    @BeforeEach
    void setUp() {
        game = DungeonFactory.createGame();
        player = game.getPlayer();

        stairwell = game.getCurrentRoom();
        lectureHall = stairwell.getExit(Direction.EAST);
        labStorage = lectureHall.getExit(Direction.EAST);
        examArchive = lectureHall.getExit(Direction.NORTH);
        deanVault = examArchive.getExit(Direction.EAST);
        finalChamber = deanVault.getExit(Direction.NORTH);
    }

    @Test
    void gameStartsWithCorrectPlayerAndQuest() {
        assertEquals("Student Explorer", player.getName());
        assertEquals(50, player.getHealth());
        assertEquals(7, player.getBaseAttack());
        assertEquals(2, player.getBaseDefense());
        assertEquals(8, player.getInventory().getCapacity());
        assertTrue(player.getInventory().getItems().isEmpty());

        assertEquals("Escape the Basement", game.getQuest().getName());
        assertEquals(QuestStatus.NOT_STARTED, game.getQuest().getStatus());
        assertFalse(game.isGameOver());
        assertFalse(game.isGameWon());
    }

    @Test
    void startingRoomIsCorrect() {
        assertEquals("stairwell", stairwell.getId());
        assertEquals("Maintenance Stairwell", stairwell.getName());
        assertSame(stairwell, player.getCurrentRoom());
        assertEquals(1, stairwell.getExits().size());
    }

    @Test
    void importantRoomsAreConnectedCorrectly() {
        assertEquals("lectureHall", lectureHall.getId());
        assertEquals("labStorage", labStorage.getId());
        assertEquals("examArchive", examArchive.getId());
        assertEquals("deanVault", deanVault.getId());
        assertEquals("finalChamber", finalChamber.getId());

        assertSame(stairwell, lectureHall.getExit(Direction.WEST));
        assertSame(lectureHall, labStorage.getExit(Direction.WEST));
        assertSame(lectureHall, examArchive.getExit(Direction.SOUTH));
        assertSame(examArchive, deanVault.getExit(Direction.WEST));
        assertSame(deanVault, finalChamber.getExit(Direction.SOUTH));

        Room serverCloset = labStorage.getExit(Direction.NORTH);
        Room elevator = lectureHall.getExit(Direction.SOUTH);

        assertEquals("serverCloset", serverCloset.getId());
        assertEquals("brokenElevator", elevator.getId());

        assertSame(labStorage, serverCloset.getExit(Direction.SOUTH));
        assertSame(deanVault, serverCloset.getExit(Direction.EAST));
        assertSame(lectureHall, elevator.getExit(Direction.NORTH));
        assertSame(serverCloset, deanVault.getExit(Direction.SOUTH));
    }

    @Test
    void importantItemsArePlacedCorrectly() {
        assertTrue(lectureHall.findItem("Coffee Potion").isPresent());
        assertTrue(labStorage.findItem("Archive Key").isPresent());
        assertTrue(labStorage.findItem("Calculator Shield").isPresent());

        Room serverCloset = labStorage.getExit(Direction.NORTH);
        assertTrue(serverCloset.findItem("Stapler of Justice").isPresent());

        assertTrue(examArchive.findItem("Lost Gradebook").isPresent());
        assertTrue(deanVault.findItem("Vault Key").isPresent());
    }

    @Test
    void lockedRoomsHaveCorrectKeys() {
        assertTrue(examArchive.isLocked());
        assertEquals("Archive Key", examArchive.getRequiredKeyName());

        assertTrue(finalChamber.isLocked());
        assertEquals("Vault Key", finalChamber.getRequiredKeyName());

        assertFalse(lectureHall.isLocked());
        assertFalse(labStorage.isLocked());
    }

    @Test
    void elevatorContainsOneTimeTrap() {
        Room elevator = lectureHall.getExit(Direction.SOUTH);
        Trap trap = elevator.getTrap();

        assertNotNull(trap);
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertTrue(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
    }

    @Test
    void allImportantMonstersArePresent() {
        assertTrue(lectureHall.findMonster("Sleep-Deprived TA").isPresent());

        Room serverCloset = labStorage.getExit(Direction.NORTH);
        assertTrue(serverCloset.findMonster("Spreadsheet Golem").isPresent());

        assertTrue(examArchive.findMonster("Plagiarism Ghost").isPresent());
        assertTrue(deanVault.findMonster("Registrar Wraith").isPresent());

        Monster boss = finalChamber
                .findMonster("Final Exam Phantom").orElseThrow();

        assertInstanceOf(BossMonster.class, boss);
        assertEquals(40, boss.getHealth());
        assertEquals(10, boss.getAttack());
        assertEquals(4, boss.getDefense());
    }

    @Test
    void playerCanUnlockArchiveWithFoundKey() {
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.move(Direction.EAST).isSuccess());

        assertTrue(game.pickUpItem("Archive Key").isSuccess());

        assertTrue(game.move(Direction.WEST).isSuccess());
        assertFalse(game.move(Direction.NORTH).isSuccess());

        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());

        assertSame(examArchive, game.getCurrentRoom());
    }

    @Test
    void collectingGradebookUpdatesQuest() {
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.pickUpItem("Archive Key").isSuccess());

        assertTrue(game.move(Direction.WEST).isSuccess());
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());

        assertTrue(game.pickUpItem("Lost Gradebook").isSuccess());

        assertTrue(game.getQuest().isGradebookRecovered());
        assertFalse(game.isGameWon());
    }

    @Test
    void creatingGamesProducesIndependentWorlds() {
        GameEngine secondGame = DungeonFactory.createGame();

        assertNotSame(game, secondGame);
        assertNotSame(game.getPlayer(), secondGame.getPlayer());
        assertNotSame(game.getQuest(), secondGame.getQuest());
        assertNotSame(game.getCurrentRoom(), secondGame.getCurrentRoom());

        game.getPlayer().takeDamage(20);
        game.move(Direction.EAST);

        assertEquals(50, secondGame.getPlayer().getHealth());
        assertEquals("stairwell", secondGame.getCurrentRoom().getId());
    }
}
