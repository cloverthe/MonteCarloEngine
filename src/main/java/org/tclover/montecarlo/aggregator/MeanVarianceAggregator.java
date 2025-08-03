package org.tclover.montecarlo.aggregator;

import org.tclover.montecarlo.core.MeanVarianceSummary;
import org.tclover.montecarlo.core.MonteCarloAggregator;

public class MeanVarianceAggregator implements MonteCarloAggregator<Double, MeanVarianceSummary> {

    private double sum = 0.0;
    private double sumSq = 0.0;
    private long count = 0;

    @Override
    public void accumulate(Double value) {
        sum += value;
        sumSq += value * value;
        count++;
    }

    @Override
    public void combine(MonteCarloAggregator<Double, MeanVarianceSummary> other) {
        if (other instanceof MeanVarianceAggregator o) {
            this.sum += o.sum;
            this.sumSq += o.sumSq;
            this.count += o.count;
        } else {
            throw new IllegalArgumentException("Incompatible aggregator type");
        }
    }

    @Override
    public MeanVarianceSummary finish(long totalSamples) {
        if (count == 0) {
            return new MeanVarianceSummary(0.0, 0.0);
        }
        double mean = sum / count;
        double variance = Math.max((sumSq / count) - (mean * mean), 0.0); // clamp negative variance to 0
        return new MeanVarianceSummary(mean, variance);
    }
}
