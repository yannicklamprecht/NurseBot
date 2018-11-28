package asylum.nursebot.modules.dices;

import java.util.Random;

public interface Distribution {
    String getName();
    String getParameters();
    void setRandom(Random random);
    double getExpectedValue();
    double getVariance();
    double generateValue();
}
