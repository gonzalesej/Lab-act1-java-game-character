import java.awt.Color;

public class Healer extends GameCharacter {
    private int healsLeft;

    public Healer(String name, int level) {
        super(name, level, 100);
        this.healsLeft = maxHeals();
    }

    private int maxHeals() { return 3 + getLevel() / 10; }

    @Override
    public String attack() {
        int dmg = roll(4 + getLevel());
        setLastDamage(dmg);
        return getName() + " strikes with a holy staff for " + dmg + " damage.";
    }

    @Override
    public String specialMove() {
        setLastDamage(0); // healing does no damage
        if (healsLeft < 1) {
            return getName() + " has no Divine Heals left.";
        }
        healsLeft -= 1;
        int amount = roll(15 + getLevel() * 3);
        heal(amount);
        return getName() + " casts Divine Heal and restores up to " + amount + " HP!";
    }

    @Override
    protected void refillResource() { healsLeft = maxHeals(); }

    @Override
    public String getType() { return "Healer"; }

    @Override
    public String getAbilityInfo() { return "Heals: " + healsLeft + "/" + maxHeals(); }

    @Override
    public Color getThemeColor() { return new Color(80, 190, 220); }
}