package org.tclover.montecarlo.example;

import org.tclover.montecarlo.aggregator.DoubleSummaryAggregator;
import org.tclover.montecarlo.core.*;
import org.tclover.montecarlo.experiment.BirthdayParadoxExperiment;

public class MainBirthday {
    public static void main(String[] args) throws Exception {
        int groupSize = 23;
        long trials = 1_000_000_000L;
        long seed = 42L;

        MonteCarloExperiment<Double> experiment = new BirthdayParadoxExperiment(groupSize, 365);
        MonteCarloSimulator<Double> simulator = new MonteCarloSimulator<>(experiment, trials, seed);

        MonteCarloAggregator<Double, MeanVarianceSummary> aggregator = new DoubleSummaryAggregator();

        MonteCarloResult<MeanVarianceSummary> result = simulator.run(aggregator);

        MeanVarianceSummary stats = result.getResult();
        double probability = stats.mean;
        double[] ci = result.getConfidenceInterval(0.95);

        System.out.printf("Birthday collision probability (n=%d): %.6f%n", groupSize, probability);
        System.out.printf("95%% Confidence Interval: [%.6f, %.6f]%n", ci[0], ci[1]);
    }
}
