package org.tclover.montecarlo.core;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Generic Monte Carlo simulation result container.
 *
 * @param <T> the type of aggregated result (e.g. Double, Map<String, Integer>, etc.)
 */
public final class MonteCarloResult<T> {
    private final T result;
    private final long samples;

    public MonteCarloResult(T result, long samples) {
        this.result = result;
        this.samples = samples;
    }

    public T getResult() {
        return result;
    }

    public long getSamples() {
        return samples;
    }

    /**
     * Computes the standard error if the result is numeric.
     *
     * @return standard error if applicable, otherwise throws
     */
    public double getStandardError() {
        if (result instanceof MeanVarianceSummary summary) {
            double safeVariance = Math.max(summary.variance, 0.0);
            if (samples <= 1 || Double.isNaN(safeVariance)) {
                return Double.NaN;
            }
            return Math.sqrt(safeVariance / samples);
        }
        throw new UnsupportedOperationException("Standard error is not supported for non-numeric results.");
    }

    public double[] getConfidenceInterval(double confidenceLevel) {
        if (result instanceof MeanVarianceSummary summary) {
            double stdErr = getStandardError();
            if (Double.isNaN(stdErr)) {
                return new double[]{Double.NaN, Double.NaN};
            }
            double z = new NormalDistribution().inverseCumulativeProbability(1 - (1 - confidenceLevel) / 2);
            double margin = z * stdErr;
            return new double[]{summary.mean - margin, summary.mean + margin};
        }
        throw new UnsupportedOperationException("Confidence interval is not supported for non-numeric results.");
    }

    public double getMean() {
        if (result instanceof MeanVarianceSummary summary) {
            return summary.mean;
        }
        throw new UnsupportedOperationException("Mean is not available for non-numeric results.");
    }

    public double getVariance() {
        if (result instanceof MeanVarianceSummary summary) {
            return summary.variance;
        }
        throw new UnsupportedOperationException("Variance is not available for non-numeric results.");
    }

    @Override
    public String toString() {
        return String.format("Samples: %d, Result: %s", samples, result);
    }
}