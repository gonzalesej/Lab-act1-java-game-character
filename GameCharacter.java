import java.awt.Color;
import java.util.Random;

public abstract class GameCharacter {
    private static final Random RNG = new Random();

    private final String name;
    private final int level;
    private final int maxHp;
    private int hp;

    // Battle record
    private int points;
    private int wins;
    private int lastDamage;

    public GameCharacter(String name, int level, int baseHp) {
        this.name = name;
        this.level = level;
        this.maxHp = baseHp + level * 10;
        this.hp = maxHp;
    }

    // Abstract methods: every character type MUST define its own version
    public abstract String attack();
    public abstract String specialMove();
    public abstract String getType();
    public abstract String getAbilityInfo();
    public abstract Color getThemeColor();

    // Hook that subclasses can override to refill mana/arrows/etc.
    protected void refillResource() { }

    // ---------- Helpers for subclasses ----------

    // Adds a little randomness (85% to 115%) so fights aren't identical every time
    protected int roll(int base) {
        return Math.max(1, (int) Math.round(base * (0.85 + RNG.nextDouble() * 0.3)));
    }

    // Subclasses call this inside attack()/specialMove() to say how much damage the move does
    protected void setLastDamage(int damage) {
        this.lastDamage = damage;
    }

    public int getLastDamage() { return lastDamage; }

    // ---------- Shared behavior ----------

    public void showName() {
        System.out.println("Character: " + name);
    }

    // Returns the HP actually lost (subclasses like Warrior can reduce it)
    public int takeDamage(int amount) {
        if (amount <= 0) return 0;
        int lost = Math.min(hp, amount);
        hp -= lost;
        return lost;
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public boolean isAlive() { return hp > 0; }

    public void rest() {
        hp = maxHp;
        refillResource();
    }

    // ---------- Points system ----------

    public void addPoints(int p) { points += p; }
    public void recordWin() { wins++; }
    public int getPoints() { return points; }
    public int getWins() { return wins; }

    public void copyRecordFrom(GameCharacter other) {
        this.points = other.points;
        this.wins = other.wins;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    @Override
    public String toString() {
        return name + " (" + getType() + " Lv." + level + ")";
    }
}