package asylum.nursebot.modules.dices;


import java.util.Random;

import org.apache.commons.math3.special.Erf;

public class NormalDistribution implements Distribution {
    private double mu;
    private double sigma;

    private Random random;

    public NormalDistribution(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String getName() {
        return "Normalverteilung";
    }

    @Override
    public String getParameters() {
        return "μ = " + mu + ", σ = " + sigma;
    }

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public double getExpectedValue() {
        return mu;
    }

    @Override
    public double getVariance() {
        return sigma * sigma;
    }

    @Override
    public double generateValue() {
        /*
         * F(x) := 1/2 * (1 + erf((x - mu) /  (2 * sigma^2)))
         * y = 1/2 * (1 + erf((x - mu) /  (2 * sigma^2)))
         * 2*y - 1 = erf((x - mu) /  (2 * sigma^2)))
         * erfInv(2*y - 1) = (x - mu) /  (2 * sigma^2))
         * 2 * sigma^2 * erfInv(2*y - 1) = x - mu
         * 2 * sigma^2 * erfInv(2*y - 1) + mu = x
         */

        double y = random.nextDouble();

        return 2 * sigma * sigma * Erf.erfInv(2*y - 1) + mu;
    }
}
