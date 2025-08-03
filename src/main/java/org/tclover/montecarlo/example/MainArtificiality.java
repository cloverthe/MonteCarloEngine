package org.tclover.montecarlo.example;

import org.tclover.montecarlo.aggregator.MeanVarianceAggregator;
import org.tclover.montecarlo.core.MeanVarianceSummary;
import org.tclover.montecarlo.core.MonteCarloSimulator;
import org.tclover.montecarlo.experiment.ArtificialityDetectionExperiment;

public class MainArtificiality {
    public static void main(String[] args) throws Exception {
        long trials = Integer.MAX_VALUE;
        long seed = 42L;

        ArtificialityDetectionExperiment.CodingMutationSpectrum cmsCovid = new ArtificialityDetectionExperiment.CodingMutationSpectrum(0.35, 0.58, 0.07);

        ArtificialityDetectionExperiment.CodingMutationSpectrum cmsFlu = new ArtificialityDetectionExperiment.CodingMutationSpectrum(0.21, 0.75, 0.04);

        var experiment = new ArtificialityDetectionExperiment(cmsCovid);
        var simulator = new MonteCarloSimulator<>(experiment, trials, seed);
        var aggregator = new MeanVarianceAggregator();

        var result = simulator.runAsync(aggregator).get();

        System.out.printf("Samples: %,d%n", result.getSamples());
        MeanVarianceSummary stats = result.getResult();
        System.out.printf("Artificiality Score Mean: %.6f%n", stats.mean);
        System.out.printf("Variance: %.6f%n", stats.variance);

        double[] ci = result.getConfidenceInterval(0.95);
        System.out.printf("95%% Confidence Interval: [%.6f, %.6f]%n", ci[0], ci[1]);

        double threshold = 0.15;
        double score = result.getMean();

        if (score >= threshold) {
            System.out.println("Conclusion: Likely ARTIFICIAL");
        } else {
            System.out.println("Conclusion: Likely NATURAL");
        }
    }
}
