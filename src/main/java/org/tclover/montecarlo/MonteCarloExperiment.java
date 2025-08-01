package org.tclover.montecarlo;

import java.util.SplittableRandom;

@FunctionalInterface
public interface MonteCarloExperiment {

    /**
     * Runs a single independent trial of the Monte Carlo simulation.
     *
     * <p>
     * This method should contain all the logic necessary to perform
     * one randomized "experiment" — whether it's sampling from a probability
     * distribution, running a physical or financial simulation, or testing a
     * hypothesis.
     * </p>
     *
     * <p>
     * The return value is a single numeric outcome from the trial. This could represent:
     * <ul>
     *   <li>A binary result (e.g., 1.0 for success, 0.0 for failure) — used to estimate probabilities</li>
     *   <li>A continuous value — used to estimate expected values, means, or integrals</li>
     * </ul>
     * </p>
     *
     * <p>
     * This interface abstracts away the details of how trials are executed.
     * It allows the simulator to treat any kind of experiment uniformly and in parallel,
     * while collecting aggregate statistics across many runs.
     * </p>
     *
     * @return the numeric result of a single simulation trial
     */
    double runTrial(SplittableRandom rnd);
}
