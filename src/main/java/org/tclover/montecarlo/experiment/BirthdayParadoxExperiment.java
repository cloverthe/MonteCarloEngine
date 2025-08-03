package org.tclover.montecarlo.experiment;

import org.tclover.montecarlo.core.MonteCarloExperiment;

import java.util.HashSet;
import java.util.SplittableRandom;

/**
 * A Monte Carlo experiment for estimating the probability that at least two people
 * in a group share the same birthday — the classic "Birthday Paradox".
 *
 * <p>
 * The result of each trial is 1.0 if a shared birthday is found, and 0.0 otherwise.
 * Repeating this many times and averaging gives an estimate of the probability.
 * </p>
 */
public class BirthdayParadoxExperiment implements MonteCarloExperiment<Double> {
    private final int groupSize;
    private final int daysInYear;

    /**
     * Creates a new experiment with a given number of people in the group.
     *
     * @param groupSize  number of people in the group (e.g. 23)
     * @param daysInYear number of possible birthdays (typically 365)
     */
    public BirthdayParadoxExperiment(int groupSize, int daysInYear) {
        this.groupSize = groupSize;
        this.daysInYear = daysInYear;
    }

    /**
     * Runs a single trial: generates random birthdays for a group and checks for duplicates.
     *
     * @param rnd the random number generator provided by the simulator
     * @return 1.0 if a duplicate birthday was found, 0.0 otherwise
     */
    @Override
    public Double runTrial(SplittableRandom rnd) {
        HashSet<Integer> seen = new HashSet<>();
        for (int i = 0; i < groupSize; i++) {
            int birthday = rnd.nextInt(daysInYear); // day 0 to 364
            if (!seen.add(birthday)) {
                return 1.0; // found duplicate
            }
        }
        return 0.0; // all unique
    }
}
