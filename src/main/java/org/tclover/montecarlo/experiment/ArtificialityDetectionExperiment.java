package org.tclover.montecarlo.experiment;

import org.tclover.montecarlo.core.MonteCarloExperiment;

import java.util.SplittableRandom;

/**
 * Monte Carlo experiment to estimate artificiality of mutation pattern.
 * Models randomness in synthetic mutation bias for realistic variance.
 */
public class ArtificialityDetectionExperiment implements MonteCarloExperiment<Double> {
    private final double naturalSilentProb;
    private final double naturalMissenseProb;
    private final double naturalNonsenseProb;

    public ArtificialityDetectionExperiment(double silent, double missense, double nonsense) {
        double total = silent + missense + nonsense;
        this.naturalSilentProb = silent / total;
        this.naturalMissenseProb = missense / total;
        this.naturalNonsenseProb = nonsense / total;
    }

    public ArtificialityDetectionExperiment(CodingMutationSpectrum cms) {
        double total = cms.getSilent() + cms.getMissense() + cms.getNonsense();
        this.naturalSilentProb = cms.getSilent() / total;
        this.naturalMissenseProb = cms.getMissense() / total;
        this.naturalNonsenseProb = cms.getNonsense() / total;
    }

    @Override
    public Double runTrial(SplittableRandom rnd) {
        // Generate synthetic mutation proportions with slight randomness
        double syntheticSilent = 0.10 + rnd.nextDouble(-0.01, 0.01);     // 0.09–0.11
        double syntheticMissense = 0.85 + rnd.nextDouble(-0.02, 0.02);   // 0.83–0.87
        double syntheticNonsense = 1.0 - syntheticSilent - syntheticMissense;

        // Clamp nonsense to a non-negative value to avoid artifacts
        syntheticNonsense = Math.max(0.0, syntheticNonsense);

        // Normalize again in case of clamping
        double total = syntheticSilent + syntheticMissense + syntheticNonsense;
        syntheticSilent /= total;
        syntheticMissense /= total;
        syntheticNonsense /= total;

        // Calculate deviation from natural
        double deltaSilent = Math.abs(syntheticSilent - naturalSilentProb);
        double deltaMissense = Math.abs(syntheticMissense - naturalMissenseProb);
        double deltaNonsense = Math.abs(syntheticNonsense - naturalNonsenseProb);

        return deltaSilent + deltaMissense + deltaNonsense;
    }

    public static class CodingMutationSpectrum {
        double silent;
        double missense;
        double nonsense;

        public CodingMutationSpectrum(double silent, double missense, double nonsense) {
            this.silent = silent;
            this.missense = missense;
            this.nonsense = nonsense;
        }

        public double getSilent() {
            return silent;
        }

        public double getMissense() {
            return missense;
        }

        public double getNonsense() {
            return nonsense;
        }
    }
}
