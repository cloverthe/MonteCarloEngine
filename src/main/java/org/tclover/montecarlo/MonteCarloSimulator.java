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
    public MonteCarloSimulator(MonteCarloExperiment experiment, long totalTrials, long seedBase) {
        this(experiment, totalTrials, seedBase, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs a simulator with a fixed number of threads.
     */
    public MonteCarloSimulator(MonteCarloExperiment experiment, long totalTrials, long seedBase, int threads) {
        this.experiment = experiment;
        this.totalTrials = totalTrials;
        this.progressCallback = null;
        this.seedBase = seedBase;
        this.threads = threads;
    }

    /**
     * Runs the simulation synchronously using optimized parallelism.
     */
    public MonteCarloResult run() throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
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
                            if (percent > lastReportedPercent.get()) {
                                if (lastReportedPercent.compareAndSet(lastReportedPercent.get(), percent)) {
                                    reportProgress((double) percent);
                                }
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
            if (progressCallback == null) {
                System.out.println();
            }

            return new MonteCarloResult(mean, variance, totalTrials);
        } finally {
            pool.shutdown(); // always shut it down
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

    private void reportProgress(double progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        } else {
            int width = 40;
            int filled = (int) (progress / 100 * width);

            StringBuilder bar = new StringBuilder();
            bar.append("\rProgress: [");
            for (int i = 0; i < width; i++) {
                bar.append(i < filled ? '=' : (i == filled ? '>' : ' '));
            }
            bar.append(String.format("] %.2f%%", progress));

            System.out.print(bar);
            System.out.flush();
        }
    }

}
