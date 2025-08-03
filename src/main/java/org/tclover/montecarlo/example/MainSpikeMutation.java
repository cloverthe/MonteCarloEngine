package org.tclover.montecarlo.example;

import org.tclover.montecarlo.aggregator.MutationStatisticsAggregator;
import org.tclover.montecarlo.core.MonteCarloResult;
import org.tclover.montecarlo.core.MonteCarloSimulator;
import org.tclover.montecarlo.core.MutationType;
import org.tclover.montecarlo.experiment.RealisticMutationExperiment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainSpikeMutation {
    public static void main(String[] args) throws Exception {
        long trials = Integer.MAX_VALUE;
        long seed = 42;

        RealisticMutationExperiment experiment =
                new RealisticMutationExperiment(RealisticMutationExperiment.loadExampleSpikeRNA());

        MonteCarloSimulator<MutationType> simulator = new MonteCarloSimulator<>(experiment, trials, seed);

        CompletableFuture<MonteCarloResult<Map<MutationType, Long>>> futureResult = simulator.runAsync(new MutationStatisticsAggregator());

        MonteCarloResult<Map<MutationType, Long>> result = futureResult.get();

        Map<MutationType, Long> counts = result.getResult();
        long total = result.getSamples();

        System.out.printf("Samples: %,d%n", total);
        for (var entry : counts.entrySet()) {
            double percent = 100.0 * entry.getValue() / total;
            System.out.printf("%-10s: %.2f%%%n", entry.getKey(), percent);
        }
    }
}
