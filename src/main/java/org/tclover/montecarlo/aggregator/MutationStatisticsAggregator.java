package org.tclover.montecarlo.aggregator;


import org.tclover.montecarlo.core.MonteCarloAggregator;
import org.tclover.montecarlo.core.MutationType;

import java.util.EnumMap;
import java.util.Map;

public class MutationStatisticsAggregator
        implements MonteCarloAggregator<MutationType, Map<MutationType, Long>> {

    private final EnumMap<MutationType, Long> counts = new EnumMap<>(MutationType.class);

    public MutationStatisticsAggregator() {
        for (MutationType type : MutationType.values()) {
            counts.put(type, 0L);
        }
    }

    @Override
    public void accumulate(MutationType value) {
        counts.merge(value, 1L, Long::sum);
    }

    @Override
    public void combine(MonteCarloAggregator<MutationType, Map<MutationType, Long>> other) {
        if (other instanceof MutationStatisticsAggregator o) {
            for (MutationType type : MutationType.values()) {
                counts.merge(type, o.counts.getOrDefault(type, 0L), Long::sum);
            }
        } else {
            throw new IllegalArgumentException("Incompatible aggregator type.");
        }
    }

    @Override
    public Map<MutationType, Long> finish(long totalSamples) {
        return counts;
    }
}
