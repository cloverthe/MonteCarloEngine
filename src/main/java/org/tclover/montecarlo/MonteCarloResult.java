package org.tclover.montecarlo;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Holds the aggregated results of a Monte Carlo simulation.
 * <p>
 * This object stores key statistical values calculated over many simulation trials:
 * <ul>
 *     <li>The sample mean — an estimate of the true expected value</li>
 *     <li>The sample variance — showing variability across trials</li>
 *     <li>The total number of trials performed</li>
 * </ul>
 * These metrics are sufficient to compute standard errors and confidence intervals
 * for the estimated result, and can be used to evaluate convergence and precision.
 * </p>
 */
public final class MonteCarloResult {
    private final double mean;
    private final double variance;
    private final long samples;

    /**
     * Constructs a result from the collected statistics.
     *
     * @param mean     the sample mean of all trials
     * @param variance the sample variance of all trial outcomes
     * @param samples  the total number of trials
     */
    public MonteCarloResult(double mean, double variance, long samples) {
        this.mean = mean;
        this.variance = variance;
        this.samples = samples;
    }

    /**
     * @return the sample mean across all simulation trials
     */
    public double getMean() {
        return mean;
    }

    /**
     * @return the sample variance across all simulation trials
     */
    public double getVariance() {
        return variance;
    }

    /**
     * @return the number of trials included in this result
     */
    public long getSamples() {
        return samples;
    }

    /**
     * Computes the standard error of the estimated mean.
     * <p>
     * This measures the expected deviation between the sample mean and the true mean
     * of the distribution being simulated. It decreases as the number of samples increases,
     * following the inverse square root law.
     * </p>
     *
     * @return the standard error (standard deviation of the mean estimate)
     */
    public double getStandardError() {
        return Math.sqrt(variance / samples);
    }

    /**
     * Calculates the confidence interval for the estimated mean result of the simulation.
     *
     * <p>
     * A confidence interval gives you a range that likely contains the true value
     * (in this case, the true average outcome of your simulation). For example,
     * if you run a Monte Carlo simulation and estimate a mean value of 0.75,
     * the 95% confidence interval might be [0.74, 0.76]. This means:
     * "We are 95% confident that the real mean lies somewhere in this range."
     * </p>
     *
     * <p>
     * The interval is calculated using the standard normal distribution (z-distribution),
     * based on the sample mean, sample variance, and the number of trials.
     * It assumes your sample size is large enough for the Central Limit Theorem to apply,
     * so the sampling distribution of the mean is approximately normal.
     * </p>
     *
     * <p>
     * The confidence level determines the width of the interval:
     * a higher level (like 99%) gives a wider interval, and a lower level (like 90%) gives a narrower one.
     * </p>
     *
     * @param confidenceLevel the desired confidence level (e.g. 0.95 for 95%)
     * @return a two-element array [lowerBound, upperBound] representing the confidence interval
     *
     * <p><b>Example:</b></p>
     * <pre>
     * double[] ci = result.getConfidenceInterval(0.95);
     * System.out.printf("95%% confidence interval: [%.6f, %.6f]%n", ci[0], ci[1]);
     * </pre>
     */
    public double[] getConfidenceInterval(double confidenceLevel) {
        double z = new NormalDistribution().inverseCumulativeProbability(1 - (1 - confidenceLevel) / 2);
        double margin = z * getStandardError();
        return new double[]{mean - margin, mean + margin};
    }

    /**
     * @return a formatted string with key statistics of the simulation result
     */
    @Override
    public String toString() {
        return String.format("Samples: %d, Mean: %.6f, Variance: %.6f", samples, mean, variance);
    }
}
