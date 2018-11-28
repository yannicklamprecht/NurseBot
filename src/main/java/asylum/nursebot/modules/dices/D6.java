package asylum.nursebot.modules.dices;

public class D6 extends StandardDice {
    public D6() {
        super(6);
    }

    private final String[] images = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

    @Override
    public String display() {
        return images[getValue() - 1];
    }
}
