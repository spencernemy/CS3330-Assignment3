# Haunted University Basement

A deterministic Java 17 Maven backend project designed for students to write JUnit tests.

## Running test requirements
In order to run mutation tests with pitest you must set your java version to jdk-21.012.1 which can be installed from java. Why? Because if everything worked on the newest version then life would be too easy.
Once installed, run:

set "PATH=C:\Program Files\Java\jdk-21.0.12.1\bin;%PATH:C:\ProgramData\Oracle\Java\javapath;=%"

in your terminal which will temporarily set the java path to that version in that terminal instance.

## Goal
Recover the **Lost Gradebook** and defeat the **Final Exam Phantom**.

## Design notes
- No console-driven gameplay loop is required for grading.
- The primary testing surface is `GameEngine`.
- Combat, traps, loot, and unlocking are deterministic.
- Students should write JUnit tests for movement, combat, inventory, traps, room unlocking, and quest progression.

## Suggested student tasks
- Write unit tests for `DamageCalculator`, `TrapResolver`, and `QuestTracker`
- Write integration tests against `GameEngine`
- Test happy paths and edge cases
- Aim for high line and branch coverage

## Main API
- `move(Direction direction)`
- `pickUpItem(String itemName)`
- `equipItem(String itemName)`
- `useItem(String itemName)`
- `unlockRoom(Direction direction)`
- `attack(String monsterName)`

## Win condition
The game is won only when both are true:
1. The player has obtained the `Lost Gradebook`
2. The `Final Exam Phantom` has been defeated
