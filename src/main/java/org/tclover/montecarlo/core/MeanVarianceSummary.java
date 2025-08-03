package org.tclover.montecarlo.core;

/**
 * Utility container for numeric summary.
 */
public class MeanVarianceSummary {
    public final double mean;
    public final double variance;

    public MeanVarianceSummary(double mean, double variance) {
        this.mean = mean;
        this.variance = variance;
    }

    @Override
    public String toString() {
        return String.format("Mean: %.6f, Variance: %.6f", mean, variance);
    }
}