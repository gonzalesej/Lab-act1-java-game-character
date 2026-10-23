# Java Game Character Manager

An improved version of the original abstraction demo. It is now a full Swing desktop app where you create characters and make them **fight each other for points**.

## Features
- **4 character types**, each with unique abilities and battle mechanics:
  - **Warrior**: armor reduces damage taken; Shield Bash needs 3 Rage (built by attacking)
  - **Mage**: mana system, Fireball and Lightning Storm, Secret Power at Lv.10+
  - **Archer**: arrow ammo, Rain of Arrows
  - **Healer**: limited Divine Heals that restore HP mid-fight
- **Battle Arena**: pick two characters and fight turn by turn (manual or Auto Battle)
  - Every HP of damage dealt = **1 point**
  - Winner gets a **+50 point bonus** and a win
  - Points and wins are saved on each character and shown in the table
- **GUI (Swing)**: dark theme, color-coded avatars with class accessories, HP bars, shake animation on hits, battle log
- **CRUD**: add, view (table), search (by name or type), update, delete
- **Undo Delete** using a stack
- **Input validation**: name required (2-20 letters/spaces), no duplicate names, level 1-99, confirmation before delete, friendly error dialogs

## How to Battle
1. Click a character in the table (fighter 1).
2. Click **BATTLE!** and choose an opponent.
3. Take turns with **Attack** / **Special**, or press **Auto Battle**.
4. The first fighter to reach 0 HP loses. Points show in the table afterward.

## Concepts Applied
| Concept | Where |
|---|---|
| Abstraction | `GameCharacter` is abstract with abstract methods `attack()`, `specialMove()`, `getType()`, `getAbilityInfo()`, `getThemeColor()` |
| Inheritance / Polymorphism | `Warrior`, `Mage`, `Archer`, `Healer` extend it; the battle calls `attacker.attack()` without knowing the type. `Warrior` also overrides `takeDamage()` for armor |
| ADT | `CharacterList` is a custom linked list; a `Deque` is used as a stack for Undo Delete |
| GUI | `Main.java` (Swing: JTable, JDialog battle window, custom-painted JPanel, Timer animation) |
| User input | Form fields, buttons, search box, opponent picker, validation |

## How to Run
Requires Java 8 or newer.

```
javac *.java
java Main
```

## Files
`GameCharacter.java`, `Warrior.java`, `Mage.java`, `Archer.java`, `Healer.java`, `CharacterList.java`, `Main.java`