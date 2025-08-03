package org.tclover.montecarlo.experiment;

import org.tclover.montecarlo.core.MonteCarloExperiment;

import java.util.SplittableRandom;

public class CoinFlipExperiment implements MonteCarloExperiment<Boolean> {
    private final double bias;

    public CoinFlipExperiment(double bias) {
        if (bias < 0.0 || bias > 1.0) {
            throw new IllegalArgumentException("Bias must be between 0.0 and 1.0");
        }
        this.bias = bias;
    }

    @Override
    public Boolean runTrial(SplittableRandom rnd) {
        return rnd.nextDouble() < bias;
    }
}
