
package com.example.haunted.rules;

import com.example.haunted.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QuestTrackerTest {
    private QuestTracker tracker;
    private Quest quest;

    @BeforeEach
    void setUp() {
        tracker = new QuestTracker();
        quest = new Quest("Find Gradebook", "");
    }

    @Test
    void nullItemDoesNotUpdateQuest() {
        tracker.updateQuestForItem(quest, null);

        assertFalse(quest.isGradebookRecovered());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Lost Gradebook",
        "lost gradebook",
        "LOST GRADEBOOK"
    })
    void correctItemUpdatesQuestRegardlessOfCase(String name) {
        tracker.updateQuestForItem(quest, new QuestItem(name, ""));

        assertTrue(quest.isGradebookRecovered());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Gradebook",
        "Lost Book",
        "Lost Gradebook ",
        "Final Exam Phantom"
    })
    void incorrectItemDoesNotUpdateQuest(String name) {
        tracker.updateQuestForItem(quest, new QuestItem(name, ""));

        assertFalse(quest.isGradebookRecovered());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void regularItemWithCorrectNameStillUpdatesQuest() {
        tracker.updateQuestForItem(quest, new Key("Lost Gradebook", ""));

        assertTrue(quest.isGradebookRecovered());
    }

    @Test
    void nullMonsterDoesNotUpdateQuest() {
        tracker.updateQuestForMonster(quest, null);

        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void livingBossDoesNotUpdateQuest() {
        Monster boss = monster("Final Exam Phantom", 10);

        tracker.updateQuestForMonster(quest, boss);

        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void damagedButLivingBossDoesNotUpdateQuest() {
        Monster boss = monster("Final Exam Phantom", 10);
        boss.takeDamage(9);

        tracker.updateQuestForMonster(quest, boss);

        assertFalse(quest.isPhantomDefeated());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Final Exam Phantom",
        "final exam phantom",
        "FINAL EXAM PHANTOM"
    })
    void defeatedBossUpdatesQuestRegardlessOfCase(String name) {
        Monster boss = monster(name, 10);
        boss.takeDamage(10);

        tracker.updateQuestForMonster(quest, boss);

        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Ghost",
        "Final Phantom",
        "Final Exam Phantom ",
        "Lost Gradebook"
    })
    void defeatingOtherMonstersDoesNotUpdateQuest(String name) {
        Monster monster = monster(name, 10);
        monster.takeDamage(10);

        tracker.updateQuestForMonster(quest, monster);

        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void defeatingBossWithExcessDamageUpdatesQuest() {
        Monster boss = monster("Final Exam Phantom", 10);
        boss.takeDamage(100);

        tracker.updateQuestForMonster(quest, boss);

        assertTrue(quest.isPhantomDefeated());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothObjectivesCompleteQuestInEitherOrder(boolean itemFirst) {
        Monster boss = monster("Final Exam Phantom", 10);
        boss.takeDamage(10);
        Item gradebook = new QuestItem("Lost Gradebook", "");

        if (itemFirst) {
            tracker.updateQuestForItem(quest, gradebook);
            assertFalse(quest.isComplete());
            tracker.updateQuestForMonster(quest, boss);
        } else {
            tracker.updateQuestForMonster(quest, boss);
            assertFalse(quest.isComplete());
            tracker.updateQuestForItem(quest, gradebook);
        }

        assertTrue(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertTrue(quest.isComplete());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void updatingCompletedQuestAgainDoesNotResetProgress() {
        tracker.updateQuestForItem(quest, new QuestItem("Lost Gradebook", ""));

        Monster boss = monster("Final Exam Phantom", 5);
        boss.takeDamage(5);
        tracker.updateQuestForMonster(quest, boss);

        tracker.updateQuestForItem(quest, new QuestItem("Lost Gradebook", ""));
        tracker.updateQuestForMonster(quest, boss);

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    private Monster monster(String name, int health) {
        return new Monster(name, health, 5, 2, List.of());
    }
}
