package asylum.nursebot.modules.dices;

public interface Dice {
    int getSides();
    int getValue();
    void roll();

    String display();
}
