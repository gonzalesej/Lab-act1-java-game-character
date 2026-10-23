import java.awt.Color;

public class Warrior extends GameCharacter {
    private int rage;

    public Warrior(String name, int level) {
        super(name, level, 120);
    }

    private int armor() { return 5 + getLevel() * 2; }

    @Override
    public String attack() {
        int dmg = roll(8 + getLevel() * 2);
        setLastDamage(dmg);
        rage = Math.min(3, rage + 1);
        return getName() + " swings a mighty sword for " + dmg + " damage! (Rage " + rage + "/3)";
    }

    @Override
    public String specialMove() {
        if (rage < 3) {
            setLastDamage(0);
            return getName() + " needs 3 Rage to use Shield Bash (has " + rage + ").";
        }
        rage = 0;
        int dmg = roll(12 + getLevel() * 3);
        setLastDamage(dmg);
        return getName() + " uses Shield Bash for " + dmg + " damage!";
    }

    // Warrior's armor absorbs part of every hit
    @Override
    public int takeDamage(int amount) {
        if (amount <= 0) return 0;
        return super.takeDamage(Math.max(1, amount - armor() / 5));
    }

    @Override
    protected void refillResource() { rage = 0; }

    @Override
    public String getType() { return "Warrior"; }

    @Override
    public String getAbilityInfo() { return "Armor " + armor() + " | Rage " + rage + "/3"; }

    @Override
    public Color getThemeColor() { return new Color(214, 69, 65); }
}