package org.tclover.montecarlo;

import java.util.SplittableRandom;

/**
 * A Monte Carlo experiment that simulates a single coin flip.
 *
 * <p>
 * Each trial returns:
 * <ul>
 *   <li>1.0 if the coin lands heads (e.g. "success")</li>
 *   <li>0.0 if it lands tails</li>
 * </ul>
 * This can be used to estimate the probability of heads or simulate repeated Bernoulli trials.
 * </p>
 */
public class CoinFlipExperiment implements MonteCarloExperiment {
    private final double bias;

    /**
     * Creates a biased or fair coin-flip experiment.
     *
     * @param bias the probability of heads (between 0.0 and 1.0).
     *             0.5 = fair coin, 0.8 = 80% chance heads, etc.
     */
    public CoinFlipExperiment(double bias) {
        if (bias < 0.0 || bias > 1.0) {
            throw new IllegalArgumentException("Bias must be between 0.0 and 1.0");
        }
        this.bias = bias;
    }

    /**
     * Simulates one coin flip using the provided random number generator.
     *
     * @param rnd the random number generator (provided by simulator)
     * @return 1.0 if heads, 0.0 if tails
     */
    @Override
    public double runTrial(SplittableRandom rnd) {
        return rnd.nextDouble() < bias ? 1.0 : 0.0;
    }
}
