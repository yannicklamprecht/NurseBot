package asylum.nursebot.modules.dices;

import java.util.Random;

public abstract class StandardDice implements Dice {
    private Random random;
    private int sides;
    private int value;

    public StandardDice(int sides) {
        random = new Random();
        this.sides = sides;
    }

    @Override
    public int getSides() {
        return sides;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void roll() {
        this.value = random.nextInt(sides) + 1;
    }

    @Override
    public String display() {
        return String.valueOf(value);
    }
}
