package org.tclover.montecarlo.experiment;

import org.tclover.montecarlo.core.MonteCarloExperiment;
import org.tclover.montecarlo.core.MutationType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class RealisticMutationExperiment implements MonteCarloExperiment<MutationType> {

    private final String rnaSequence;
    private final List<String> codons;
    private final Map<String, String> codonTable;

    public RealisticMutationExperiment(String fullRnaSequence) {
        this.rnaSequence = fullRnaSequence.toUpperCase().replace("T", "U");
        this.codons = splitIntoCodons(this.rnaSequence);
        this.codonTable = buildCodonTable();
    }

    private static List<String> splitIntoCodons(String seq) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i + 2 < seq.length(); i += 3) {
            result.add(seq.substring(i, i + 3));
        }
        return result;
    }


    private static Map<String, String> buildCodonTable() {
        return Map.<String, String>ofEntries(
                Map.entry("UUU", "F"), Map.entry("UUC", "F"), Map.entry("UUA", "L"), Map.entry("UUG", "L"),
                Map.entry("CUU", "L"), Map.entry("CUC", "L"), Map.entry("CUA", "L"), Map.entry("CUG", "L"),
                Map.entry("AUU", "I"), Map.entry("AUC", "I"), Map.entry("AUA", "I"), Map.entry("AUG", "M"),
                Map.entry("GUU", "V"), Map.entry("GUC", "V"), Map.entry("GUA", "V"), Map.entry("GUG", "V"),
                Map.entry("UCU", "S"), Map.entry("UCC", "S"), Map.entry("UCA", "S"), Map.entry("UCG", "S"),
                Map.entry("CCU", "P"), Map.entry("CCC", "P"), Map.entry("CCA", "P"), Map.entry("CCG", "P"),
                Map.entry("ACU", "T"), Map.entry("ACC", "T"), Map.entry("ACA", "T"), Map.entry("ACG", "T"),
                Map.entry("GCU", "A"), Map.entry("GCC", "A"), Map.entry("GCA", "A"), Map.entry("GCG", "A"),
                Map.entry("UAU", "Y"), Map.entry("UAC", "Y"), Map.entry("UAA", "*"), Map.entry("UAG", "*"),
                Map.entry("CAU", "H"), Map.entry("CAC", "H"), Map.entry("CAA", "Q"), Map.entry("CAG", "Q"),
                Map.entry("AAU", "N"), Map.entry("AAC", "N"), Map.entry("AAA", "K"), Map.entry("AAG", "K"),
                Map.entry("GAU", "D"), Map.entry("GAC", "D"), Map.entry("GAA", "E"), Map.entry("GAG", "E"),
                Map.entry("UGU", "C"), Map.entry("UGC", "C"), Map.entry("UGA", "*"), Map.entry("UGG", "W"),
                Map.entry("CGU", "R"), Map.entry("CGC", "R"), Map.entry("CGA", "R"), Map.entry("CGG", "R"),
                Map.entry("AGU", "S"), Map.entry("AGC", "S"), Map.entry("AGA", "R"), Map.entry("AGG", "R"),
                Map.entry("GGU", "G"), Map.entry("GGC", "G"), Map.entry("GGA", "G"), Map.entry("GGG", "G")
        );
    }

    private static String loadFasta(String resourcePath) throws IOException {
        InputStream in = RealisticMutationExperiment.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("FASTA file not found: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith(">"))
                    .collect(Collectors.joining())
                    .toUpperCase()
                    .replace('T', 'U');
        }
    }

    public static String loadExampleSpikeRNA() throws IOException {
        return loadFasta("/spike_referenceGenome.fasta");
    }

    public static String loadExampleSarsRNA() throws IOException {
        return loadFasta("/sars_fullGenome.fasta");
    }

    public static String loadOC43() throws IOException {
        return loadFasta("/oc43_fullGenome.fasta");
    }



    private static char biasedMutation(char original, SplittableRandom rnd) {
        double r = rnd.nextDouble();
        return switch (original) {
            case 'A' -> r < 0.50 ? 'G' : r < 0.75 ? 'U' : 'C';
            case 'C' -> r < 0.70 ? 'U' : r < 0.85 ? 'A' : 'G';
            case 'G' -> r < 0.45 ? 'A' : r < 0.75 ? 'U' : 'C';
            case 'U' -> r < 0.45 ? 'C' : r < 0.75 ? 'A' : 'G';
            default  -> "ACGU".charAt(rnd.nextInt(4));
        };
    }
    @Override
    public MutationType runTrial(SplittableRandom rnd) {
        int codonIndex = rnd.nextInt(codons.size());
        String originalCodon = codons.get(codonIndex);

        while (true) {
            char[] codon = originalCodon.toCharArray();
            int mutateIndex = rnd.nextInt(3);
            char originalBase = codon[mutateIndex];
            char newBase = biasedMutation(originalBase, rnd);
            if (newBase == originalBase) continue;
            codon[mutateIndex] = newBase;
            String mutatedCodon = new String(codon);

            String originalAA = codonTable.get(originalCodon);
            String mutatedAA = codonTable.get(mutatedCodon);

            if (originalAA == null || mutatedAA == null) {
                return MutationType.SILENT;
            }

            MutationType type;
            if (mutatedAA.equals("*")) {
                type = MutationType.NONSENSE;
            } else if (!mutatedAA.equals(originalAA)) {
                type = MutationType.MISSENSE;
            } else {
                type = MutationType.SILENT;
            }

            // Отбор: если мутация не выживает — пробуем другую мутацию
            double survivalProb = switch (type) {
                case SILENT   -> 1.00;
                case MISSENSE -> 0.55;
                case NONSENSE -> 0.08;
            };
            if (rnd.nextDouble() < survivalProb) return type;
        }
    }
}
