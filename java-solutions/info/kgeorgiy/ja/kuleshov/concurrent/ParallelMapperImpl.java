package info.kgeorgiy.ja.kuleshov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadList;
    private final Queue<Runnable> tasks;
    private final int threads;

    public ParallelMapperImpl(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("expected threads > 0, but found: " + threads);
        }
        tasks = new ArrayDeque<>();
        this.threads = threads;
        threadList = new ArrayList<>(threads);
        Runnable thread = () -> {
            while (true) {
                Runnable task;
                synchronized (tasks) {
                    try {
                        while (tasks.isEmpty()) {
                            tasks.wait();
                        }
                    }catch (InterruptedException ignored) {
                        return;
                    }
                    task = tasks.poll();
                }
                task.run();
            }
        };
        for (int threadIndex = 0; threadIndex < threads; ++threadIndex) {
            threadList.add(new Thread(thread));
            threadList.get(threadIndex).start();
        }
    }

    private static class Counter {
        private int value;

        public Counter(int value) {
            this.value = value;
        }

        public void decrement() {
            value--;
        }

        public boolean isZero() {
            return value == 0;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final int groupNumber = Integer.min(threads, args.size());
        final List<List<? extends T>> groups = IterativeParallelism.cutList(groupNumber, args);
        final List<List<? extends R>> groupResults = new ArrayList<>(Collections.nCopies(groupNumber, null));
        final Counter notFinishedTasks = new Counter(groupNumber);
        synchronized (tasks) {
            for (int groupIndex = 0; groupIndex < groupNumber; ++groupIndex) {
                final int finalGroupIndex = groupIndex;
                tasks.add(() -> {
                    groupResults.set(finalGroupIndex, groups.get(finalGroupIndex)
                            .stream()
                            .map(f)
                            .collect(Collectors.toList()));
                    synchronized (notFinishedTasks) {
                        notFinishedTasks.decrement();
                        if (notFinishedTasks.isZero()) {
                            notFinishedTasks.notify();
                        }
                    }
                });
                tasks.notify();
            }
        }
        while (!notFinishedTasks.isZero()) {
            synchronized (notFinishedTasks) {
                notFinishedTasks.wait();
            }
        }
        return groupResults.stream()
                .map(Collection::stream)
                .reduce(Stream::concat)
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        threadList.forEach(Thread::interrupt);
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {

            }
        }
    }
}