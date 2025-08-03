package org.tclover.montecarlo;

import java.util.concurrent.CompletableFuture;

/**
 * Estimates the value of π using a Monte Carlo simulation.
 */
public class MainPi {
    public static void main(String[] args) throws Exception {
        long trials = Integer.MAX_VALUE;
        long seed = 1234;

        MonteCarloExperiment experiment = new PiEstimationExperiment();
        MonteCarloSimulator simulator = new MonteCarloSimulator(experiment, trials, seed);

        CompletableFuture<MonteCarloResult> futureResult = simulator.runAsync();

        MonteCarloResult result = futureResult.get();

        double[] ci = result.getConfidenceInterval(0.95);
        double piEstimate = 4 * result.getMean();
        System.out.printf("Estimated π = %.8f%n", piEstimate);
        System.out.printf("True π: %.8f%n", Math.PI);
        System.out.printf("95%% CI for π: [%.8f, %.8f]%n", 4 * ci[0], 4 * ci[1]);
    }
}
