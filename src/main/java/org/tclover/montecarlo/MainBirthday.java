package org.tclover.montecarlo;

/**
 * Example of birthday paradox calculations
 */
public class MainBirthday {
    public static void main(String[] args) throws Exception {
        int groupSize = 23;
        long trials = 100_000_000;
        long seed = 42L;
        int threads = Runtime.getRuntime().availableProcessors();


        MonteCarloExperiment experiment = new BirthdayParadoxExperiment(groupSize, 365);

        MonteCarloSimulator simulator = new MonteCarloSimulator(
                experiment,
                trials,
                progress -> System.out.printf("Progress: %.2f%%%n", progress),
                seed,
                threads
        );

        MonteCarloResult result = simulator.run();
        double probability = result.getMean();
        double[] ci = result.getConfidenceInterval(0.95);

        System.out.printf("Birthday collision probability (n=%d): %.6f%n", groupSize, probability);
        System.out.printf("95%% Confidence Interval: [%.6f, %.6f]%n", ci[0], ci[1]);
    }
}
