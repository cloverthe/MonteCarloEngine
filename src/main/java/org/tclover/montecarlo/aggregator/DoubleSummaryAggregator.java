package org.tclover.montecarlo.aggregator;

import org.tclover.montecarlo.core.MeanVarianceSummary;
import org.tclover.montecarlo.core.MonteCarloAggregator;

public class DoubleSummaryAggregator implements MonteCarloAggregator<Double, MeanVarianceSummary> {
    private double sum = 0;
    private double sumSq = 0;
    private long count = 0;

    @Override
    public void accumulate(Double value) {
        sum += value;
        sumSq += value * value;
        count++;
    }

    @Override
    public void combine(MonteCarloAggregator<Double, MeanVarianceSummary> other) {
        if (other instanceof DoubleSummaryAggregator o) {
            this.sum += o.sum;
            this.sumSq += o.sumSq;
            this.count += o.count;
        }
    }

    @Override
    public MeanVarianceSummary finish(long totalSamples) {
        double mean = sum / count;
        double variance = (sumSq / count) - (mean * mean);
        return new MeanVarianceSummary(mean, variance);
    }
}
