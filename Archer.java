import java.awt.Color;

public class Archer extends GameCharacter {
    private int arrows;

    public Archer(String name, int level) {
        super(name, level, 90);
        this.arrows = maxArrows();
    }

    private int maxArrows() { return 15 + getLevel(); }

    @Override
    public String attack() {
        if (arrows < 1) {
            setLastDamage(2);
            return getName() + " has no arrows left and kicks the enemy (2 damage).";
        }
        arrows -= 1;
        int dmg = roll(7 + getLevel() * 2);
        setLastDamage(dmg);
        return getName() + " fires an arrow for " + dmg + " damage! (-1 arrow)";
    }

    @Override
    public String specialMove() {
        if (arrows < 5) {
            setLastDamage(0);
            return getName() + " needs 5 arrows for Rain of Arrows.";
        }
        arrows -= 5;
        int dmg = roll(15 + getLevel() * 3);
        setLastDamage(dmg);
        return getName() + " calls down a Rain of Arrows for " + dmg + " damage! (-5 arrows)";
    }

    @Override
    protected void refillResource() { arrows = maxArrows(); }

    @Override
    public String getType() { return "Archer"; }

    @Override
    public String getAbilityInfo() { return "Arrows: " + arrows + "/" + maxArrows(); }

    @Override
    public Color getThemeColor() { return new Color(60, 170, 90); }
}