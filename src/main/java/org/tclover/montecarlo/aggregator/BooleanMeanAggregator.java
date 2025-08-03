package org.tclover.montecarlo.aggregator;

import org.tclover.montecarlo.core.MeanVarianceSummary;
import org.tclover.montecarlo.core.MonteCarloAggregator;

public class BooleanMeanAggregator implements MonteCarloAggregator<Boolean, MeanVarianceSummary> {
    private long count = 0;
    private long sum = 0;

    public BooleanMeanAggregator() {
    }

    @Override
    public void accumulate(Boolean value) {
        count++;
        if (value) sum++;
    }

    @Override
    public void combine(MonteCarloAggregator<Boolean, MeanVarianceSummary> other) {
        if (other instanceof BooleanMeanAggregator o) {
            this.count += o.count;
            this.sum += o.sum;
        } else {
            throw new IllegalArgumentException("Incompatible aggregator");
        }
    }

    @Override
    public MeanVarianceSummary finish(long totalSamples) {
        double mean = sum / (double) count;
        double variance = mean * (1.0 - mean); // Bernoulli variance
        return new MeanVarianceSummary(mean, variance);
    }
}
