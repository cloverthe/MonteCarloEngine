package org.tclover.montecarlo.example;

import org.tclover.montecarlo.aggregator.DoubleSummaryAggregator;
import org.tclover.montecarlo.core.*;
import org.tclover.montecarlo.experiment.PiEstimationExperiment;

import java.util.concurrent.CompletableFuture;

public class MainPi {
    public static void main(String[] args) throws Exception {
        long trials = Integer.MAX_VALUE;
        long seed = 1234;

        MonteCarloExperiment<Double> experiment = new PiEstimationExperiment();
        MonteCarloSimulator<Double> simulator = new MonteCarloSimulator<>(experiment, trials, seed);

        // Use an aggregator that computes mean + variance
        MonteCarloAggregator<Double, MeanVarianceSummary> aggregator = new DoubleSummaryAggregator();

        CompletableFuture<MonteCarloResult<MeanVarianceSummary>> futureResult =
                simulator.runAsync(aggregator);

        MonteCarloResult<MeanVarianceSummary> result = futureResult.get();
        MeanVarianceSummary summary = result.getResult();

        double mean = summary.mean;
        double piEstimate = 4 * mean;
        double[] ci = result.getConfidenceInterval(0.95);

        System.out.printf("Estimated π = %.8f%n", piEstimate);
        System.out.printf("True π: %.8f%n", Math.PI);
        System.out.printf("95%% CI for π: [%.8f, %.8f]%n", 4 * ci[0], 4 * ci[1]);
    }
}
