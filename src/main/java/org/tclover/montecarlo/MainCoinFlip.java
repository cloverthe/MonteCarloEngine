package org.tclover.montecarlo;

import java.util.concurrent.CompletableFuture;

/**
 * Estimates the probability of each coin side.
 */
public class MainCoinFlip {
    public static void main(String[] args) throws Exception {
        long trials = 1000_000_000;
        long seed = 1234;

        MonteCarloExperiment experiment = new CoinFlipExperiment(0.5);
        MonteCarloSimulator simulator = new MonteCarloSimulator(experiment, trials, seed);

        CompletableFuture<MonteCarloResult> futureResult = simulator.runAsync();

        MonteCarloResult result = futureResult.get();
        System.out.printf("Estimated probability of heads: %.5f%n", result.getMean());
        System.out.printf("Estimated probability of tails: %.5f%n", 1.0 - result.getMean());
        double[] ci = result.getConfidenceInterval(0.95);
        System.out.printf("95%% CI for heads: [%.5f, %.5f]%n", ci[0], ci[1]);
        System.out.printf("95%% CI for tails: [%.5f, %.5f]%n", 1.0 - ci[1], 1.0 - ci[0]);
    }
}
