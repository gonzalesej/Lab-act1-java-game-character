import java.awt.Color;

public class Mage extends GameCharacter {
    private int mana;

    public Mage(String name, int level) {
        super(name, level, 80);
        this.mana = maxMana();
    }

    private int maxMana() { return 50 + getLevel() * 5; }

    @Override
    public String attack() {
        if (mana < 5) {
            setLastDamage(2);
            return getName() + " is out of mana and can only poke with a staff (2 damage).";
        }
        mana -= 5;
        int dmg = roll(5 + getLevel() * 2);
        setLastDamage(dmg);
        return getName() + " casts a fireball for " + dmg + " damage! (-5 mana)";
    }

    @Override
    public String specialMove() {
        if (mana < 20) {
            setLastDamage(0);
            return getName() + " doesn't have enough mana for Lightning Storm (needs 20).";
        }
        mana -= 20;
        int dmg = roll(15 + getLevel() * 4);
        setLastDamage(dmg);
        String msg = getName() + " uses Lightning Storm for " + dmg + " damage! (-20 mana)";
        if (getLevel() >= 10) {
            msg += " " + secretPower();
        }
        return msg;
    }

    // Unique to Mage (from the original project)
    public String secretPower() {
        return getName() + " unleashes the Secret Power: Utot!!!";
    }

    @Override
    protected void refillResource() { mana = maxMana(); }

    @Override
    public String getType() { return "Mage"; }

    @Override
    public String getAbilityInfo() { return "Mana: " + mana + "/" + maxMana(); }

    @Override
    public Color getThemeColor() { return new Color(130, 90, 220); }
}