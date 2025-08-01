package org.tclover.montecarlo;

import java.util.SplittableRandom;

/**
 * A Monte Carlo experiment for estimating the value of π (pi).
 *
 * <p>
 * This simulation is based on the classic geometric approach:
 * random points are generated uniformly inside the unit square [0,1]×[0,1],
 * and the fraction of points that fall inside the quarter-circle of radius 1
 * (i.e. points satisfying x² + y² ≤ 1) approximates the area of the quarter-circle.
 * </p>
 *
 * <p>
 * Since the area of the quarter-circle is (π / 4), the ratio of "hits" to total points
 * can be used to estimate π as:
 * <pre>
 *     π ≈ 4 × (number of hits / total samples)
 * </pre>
 * </p>
 *
 * <p>
 * This class returns 1.0 if a generated point lies inside the quarter-circle, and 0.0 otherwise.
 * The Monte Carlo simulator will compute the average of many such trials, which converges to π/4.
 * </p>
 */
public class PiEstimationExperiment implements MonteCarloExperiment {


    /**
     * Constructs a pi-estimating experiment using a fixed random seed for reproducibility.
     */
    public PiEstimationExperiment() {

    }

    /**
     * Runs a single trial by generating a random point (x, y) in the unit square
     * and checking if it lies inside the unit quarter-circle.
     *
     * @return 1.0 if the point is inside the circle (a "hit"), 0.0 otherwise
     */
    @Override
    public double runTrial(SplittableRandom rnd) {
        double x = rnd.nextDouble();
        double y = rnd.nextDouble();
        return (x * x + y * y <= 1.0) ? 1.0 : 0.0;
    }
}
