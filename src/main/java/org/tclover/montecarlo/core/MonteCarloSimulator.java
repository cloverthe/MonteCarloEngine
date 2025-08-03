package org.tclover.montecarlo.core;

import java.util.SplittableRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MonteCarloSimulator<T> {
    private final MonteCarloExperiment<T> experiment;
    private final long totalTrials;
    private final Consumer<Double> progressCallback;
    private final int threads;
    private final long seedBase;
    private final AtomicLong lastReportedPercent = new AtomicLong(-1);

    public MonteCarloSimulator(MonteCarloExperiment<T> experiment, long totalTrials, long seedBase) {
        this(experiment, totalTrials, seedBase, Runtime.getRuntime().availableProcessors());
    }

    public MonteCarloSimulator(MonteCarloExperiment<T> experiment, long totalTrials, long seedBase, int threads) {
        this.experiment = experiment;
        this.totalTrials = totalTrials;
        this.progressCallback = null;
        this.seedBase = seedBase;
        this.threads = threads;
    }

    public <R> MonteCarloResult<R> run(MonteCarloAggregator<T, R> prototypeAggregator) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicLong completed = new AtomicLong(0);
        @SuppressWarnings("unchecked")
        MonteCarloAggregator<T, R>[] partials = new MonteCarloAggregator[threads];

        long trialsPerThread = totalTrials / threads;
        long remainder = totalTrials % threads;

        for (int i = 0; i < threads; i++) {
            final int threadIndex = i;
            final long trials = trialsPerThread + (i < remainder ? 1 : 0);
            final MonteCarloAggregator<T, R> localAgg = createAggregatorInstance(prototypeAggregator);
            partials[threadIndex] = localAgg;

            pool.execute(() -> {
                SplittableRandom rnd = new SplittableRandom(seedBase + threadIndex);
                long localCompleted = 0;
                for (long j = 0; j < trials; j++) {
                    T value = experiment.runTrial(rnd);
                    localAgg.accumulate(value);
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
                completed.addAndGet(trials - localCompleted);
                latch.countDown();
            });
        }

        latch.await();
        pool.shutdown();
        reportProgress(100.0);
        MonteCarloAggregator<T, R> finalAgg = createAggregatorInstance(prototypeAggregator);
        for (MonteCarloAggregator<T, R> part : partials) {
            finalAgg.combine(part);
        }

        if (progressCallback == null) {
            System.out.println();
        }

        R finalResult = finalAgg.finish(totalTrials);
        return new MonteCarloResult<>(finalResult, totalTrials);
    }

    public <R> CompletableFuture<MonteCarloResult<R>> runAsync(MonteCarloAggregator<T, R> aggregator) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return run(aggregator);
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <R> MonteCarloAggregator<T, R> createAggregatorInstance(MonteCarloAggregator<T, R> prototype) {
        try {
            return (MonteCarloAggregator<T, R>) prototype.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Aggregator must have public no-arg constructor", e);
        }
    }

    private void reportProgress(double progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        } else {
            int width = 50;
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