package com.example.haunted.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.haunted.engine.GameEngine;

class DungeonFactoryTest {

	@Test
	void testGameCreation() {
		GameEngine game = DungeonFactory.createGame();
		assertNotNull(game);
	}
	
	@Test
	void testPlayerHealth() {
		GameEngine game = DungeonFactory.createGame();
		assertEquals(50, game.getPlayer().getHealth());
	}
	
	@Test
	void testStartingRoom() {
		GameEngine game = DungeonFactory.createGame();
		assertEquals("stairwell", game.getCurrentRoom().getId());
	}

}
