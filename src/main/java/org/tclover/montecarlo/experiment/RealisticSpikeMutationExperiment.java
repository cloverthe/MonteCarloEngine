package org.tclover.montecarlo.experiment;

import org.tclover.montecarlo.core.MonteCarloExperiment;
import org.tclover.montecarlo.core.MutationType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A realistic Monte Carlo simulation for modeling point mutations
 * in the mRNA of the SARS-CoV-2 spike protein.
 */
public class RealisticSpikeMutationExperiment implements MonteCarloExperiment<MutationType> {

    private final String rnaSequence;
    private final List<String> codons;
    private final Map<String, String> codonTable;

    public RealisticSpikeMutationExperiment(String fullRnaSequence) {
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

    private static char randomBase(SplittableRandom rnd) {
        return "ACGU".charAt(rnd.nextInt(4));
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

    public static String loadExampleSpikeRNA() throws IOException {
        String sequence;
        try (InputStream in = RealisticSpikeMutationExperiment.class
                .getResourceAsStream("/spike_referenceGenome.fasta")) {

            if (in == null) throw new RuntimeException("Failed to load FASTA");

            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            sequence = Arrays.stream(content.split("\\R")) // split by \r, \n, \r\n
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith(">"))
                    .collect(Collectors.joining())
                    .toUpperCase()
                    .replace("T", "U");
        }
        return sequence;

    }

    @Override
    public MutationType runTrial(SplittableRandom rnd) {
        int codonIndex = rnd.nextInt(codons.size());
        String originalCodon = codons.get(codonIndex);

        char[] codon = originalCodon.toCharArray();
        int mutateIndex = rnd.nextInt(3);
        char originalBase = codon[mutateIndex];
        char newBase;
        do {
            newBase = randomBase(rnd);
        } while (newBase == originalBase);
        codon[mutateIndex] = newBase;
        String mutatedCodon = new String(codon);

        String originalAA = codonTable.getOrDefault(originalCodon, "?");
        String mutatedAA = codonTable.getOrDefault(mutatedCodon, "?");

        if (mutatedAA.equals("*")) {
            return MutationType.NONSENSE;
        } else if (!mutatedAA.equals(originalAA)) {
            return MutationType.MISSENSE;
        } else {
            return MutationType.SILENT;
        }
    }

}
