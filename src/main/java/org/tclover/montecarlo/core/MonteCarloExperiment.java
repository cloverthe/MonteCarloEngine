package org.tclover.montecarlo.core;

import java.util.SplittableRandom;

/**
 * A generic interface for Monte Carlo experiments producing results of any type.
 *
 * @param <T> the result type of a single simulation trial
 */
@FunctionalInterface
public interface MonteCarloExperiment<T> {

    /**
     * Executes a single trial of the Monte Carlo experiment.
     *
     * @param rnd a source of randomness
     * @return the result of the trial
     */
    T runTrial(SplittableRandom rnd);
}