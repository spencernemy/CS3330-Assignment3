
package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class QuestTest {

    @Test
    void questStartsNotStarted() {
        Quest quest = new Quest("Escape", "Find the gradebook");

        assertEquals("Escape", quest.getName());
        assertEquals("Find the gradebook", quest.getDescription());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
        assertFalse(quest.isComplete());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void updatingQuestWithoutProgressChangesNothing() {
        Quest quest = new Quest("Escape", "");

        quest.updateStatus();

        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void oneObjectiveOnlyMakesQuestInProgress(boolean gradebookFirst) {
        Quest quest = new Quest("Escape", "");

        if (gradebookFirst) {
            quest.markGradebookRecovered();
        } else {
            quest.markPhantomDefeated();
        }

        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothObjectivesCompleteQuestInEitherOrder(boolean gradebookFirst) {
        Quest quest = new Quest("Escape", "");

        if (gradebookFirst) {
            quest.markGradebookRecovered();
            quest.markPhantomDefeated();
        } else {
            quest.markPhantomDefeated();
            quest.markGradebookRecovered();
        }

        assertTrue(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertTrue(quest.isComplete());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void repeatingObjectivesDoesNotResetCompletion() {
        Quest quest = new Quest("Escape", "");
        quest.markGradebookRecovered();
        quest.markPhantomDefeated();

        quest.markGradebookRecovered();
        quest.markPhantomDefeated();
        quest.updateStatus();

        assertTrue(quest.isComplete());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }
}
