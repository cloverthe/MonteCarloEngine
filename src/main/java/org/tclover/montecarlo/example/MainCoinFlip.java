package org.tclover.montecarlo.example;

import org.tclover.montecarlo.aggregator.BooleanMeanAggregator;
import org.tclover.montecarlo.core.*;
import org.tclover.montecarlo.experiment.CoinFlipExperiment;

import java.util.concurrent.CompletableFuture;

public class MainCoinFlip {
    public static void main(String[] args) throws Exception {
        long trials = 1_000_000_000L;
        long seed = 1234;

        MonteCarloExperiment<Boolean> experiment = new CoinFlipExperiment(0.5);
        MonteCarloSimulator<Boolean> simulator = new MonteCarloSimulator<>(experiment, trials, seed);
        MonteCarloAggregator<Boolean, MeanVarianceSummary> aggregator = new BooleanMeanAggregator();

        CompletableFuture<MonteCarloResult<MeanVarianceSummary>> futureResult = simulator.runAsync(aggregator);
        MonteCarloResult<MeanVarianceSummary> result = futureResult.get();

        double mean = result.getResult().mean;
        double[] ci = result.getConfidenceInterval(0.95);

        System.out.printf("Estimated probability of heads: %.5f%n", mean);
        System.out.printf("Estimated probability of tails: %.5f%n", 1.0 - mean);
        System.out.printf("95%% CI for heads: [%.5f, %.5f]%n", ci[0], ci[1]);
        System.out.printf("95%% CI for tails: [%.5f, %.5f]%n", 1.0 - ci[1], 1.0 - ci[0]);
    }
}
