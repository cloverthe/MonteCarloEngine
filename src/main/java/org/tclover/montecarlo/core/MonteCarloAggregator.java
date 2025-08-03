package org.tclover.montecarlo.core;

public interface MonteCarloAggregator<T, R> {
    void accumulate(T value);

    void combine(MonteCarloAggregator<T, R> other);

    R finish(long totalSamples);
}
