package org.tclover.montecarlo;

import java.util.SplittableRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Consumer;

/**
 * A highly optimized multithreaded Monte Carlo simulation engine.
 * <p>
 * This version uses ForkJoinPool, SplittableRandom, and low-contention adders
 * to achieve maximum performance on multi-core systems.
 * </p>
 */
public class MonteCarloSimulator {
    private final MonteCarloExperiment experiment;
    private final long totalTrials;
    private final Consumer<Double> progressCallback;
    private final int threads;
    private final long seedBase;
    private final AtomicLong lastReportedPercent = new AtomicLong(-1);

    /**
     * Constructs a simulator using all available processors.
     */
    public MonteCarloSimulator(MonteCarloExperiment experiment, long totalTrials,
                               Consumer<Double> progressCallback, long seedBase) {
        this(experiment, totalTrials, progressCallback, seedBase, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs a simulator with a fixed number of threads.
     */
    public MonteCarloSimulator(MonteCarloExperiment experiment, long totalTrials,
                               Consumer<Double> progressCallback, long seedBase, int threads) {
        this.experiment = experiment;
        this.totalTrials = totalTrials;
        this.progressCallback = progressCallback;
        this.seedBase = seedBase;
        this.threads = threads;
    }

    /**
     * Runs the simulation synchronously using optimized parallelism.
     */
    public MonteCarloResult run() throws InterruptedException {
        try (ForkJoinPool pool = new ForkJoinPool(threads)) {
            DoubleAdder totalSum = new DoubleAdder();
            DoubleAdder totalSumSq = new DoubleAdder();
            AtomicLong completed = new AtomicLong(0);
            CountDownLatch latch = new CountDownLatch(threads);

            long trialsPerThread = totalTrials / threads;
            long remainder = totalTrials % threads;

            for (int i = 0; i < threads; i++) {
                final int threadIndex = i;
                final long trials = trialsPerThread + (i < remainder ? 1 : 0);

                pool.execute(() -> {
                    SplittableRandom rnd = new SplittableRandom(seedBase + threadIndex);
                    double localSum = 0.0, localSumSq = 0.0;
                    long localCompleted = 0;

                    for (long j = 0; j < trials; j++) {
                        double val = experiment.runTrial(rnd);
                        localSum += val;
                        localSumSq += val * val;

                        localCompleted++;
                        if ((localCompleted % 100_000) == 0) {
                            long done = completed.addAndGet(100_000);
                            long percent = (done * 100) / totalTrials;
                            if (progressCallback != null && percent > lastReportedPercent.get()) {
                                lastReportedPercent.updateAndGet(prev -> {
                                    if (percent > prev) {
                                        progressCallback.accept((double) percent);
                                        return percent;
                                    }
                                    return prev;
                                });
                            }
                        }
                    }

                    totalSum.add(localSum);
                    totalSumSq.add(localSumSq);
                    completed.addAndGet(trials - localCompleted); // final increment
                    latch.countDown();
                });
            }

            latch.await();

            double mean = totalSum.sum() / totalTrials;
            double variance = (totalSumSq.sum() / totalTrials) - (mean * mean);
            return new MonteCarloResult(mean, variance, totalTrials);
        }
    }

    /**
     * Runs the simulation asynchronously using a CompletableFuture.
     */
    public CompletableFuture<MonteCarloResult> runAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return run();
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }
}
