package info.kgeorgiy.ja.kuleshov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.min;

public class IterativeParallelism implements ListIP {
    private ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism() {
    }

    public static <E> List<List<? extends E>> cutList(int groups, List<? extends E> list) {
        List<List<? extends E>> result = new ArrayList<>();
        final int groupSize = list.size() / groups;
        int modGroupSize = list.size() % groups;
        int currentLeftIndex = 0;
        for (int groupIndex = 0; groupIndex < groups; ++groupIndex) {
            int currentGroupSize = groupSize;
            if (modGroupSize > 0) {
                currentGroupSize++;
                modGroupSize--;
            }
            result.add(list.subList(currentLeftIndex, currentLeftIndex + currentGroupSize));
            currentLeftIndex += currentGroupSize;
        }
        return result;
    }

    public <T, U> U parallelFunc(int threads, List<? extends T> values,
                                 Function<List<? extends T>, ? extends U> function,
                                 Function<List<? extends U>, ? extends U> resultFunction) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("expected threads > 0, but found: " + threads);
        }
        if (values.size() == 0) {
            return resultFunction.apply(new ArrayList<>(Collections.nCopies(1, function.apply(values))));
        }
        final int groupNumber = min(threads, values.size());
        List<List<? extends T>> groups = cutList(groupNumber, values);
        if (parallelMapper != null) {
            return resultFunction.apply(parallelMapper.map(function, groups));
        }
        List<U> results = new ArrayList<>(Collections.nCopies(groupNumber, null));
        List<Thread> threadList = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < groupNumber; ++groupIndex) {
            final int finalGroupIndex = groupIndex;
            threadList.add(new Thread(() -> results.set(finalGroupIndex, function.apply(groups.get(finalGroupIndex)))));
            threadList.get(groupIndex).start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        return resultFunction.apply(results);
    }

    private static <T> String joining(List<T> list) {
        return list
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelFunc(
                threads,
                values,
                IterativeParallelism::joining,
                IterativeParallelism::joining);
    }

    private static <T> List<T> concatLists(List<? extends List<T>> lists) {
        return lists
                .stream()
                .map(Collection::stream)
                .reduce(Stream::concat)
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFunc(
                threads,
                values,
                (list) -> list
                        .stream()
                        .filter(predicate)
                        .collect(Collectors.toList()),
                IterativeParallelism::concatLists);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelFunc(
                threads,
                values,
                (list) -> list
                        .stream()
                        .map(f)
                        .collect(Collectors.toList()),
                IterativeParallelism::concatLists);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        T result;
        try {
            result = minimum(threads, values, comparator.reversed());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("no maximum");
        }
        return result;
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        T result = parallelFunc(
                threads,
                values,
                (list) -> list
                        .stream()
                        .min(comparator)
                        .orElse(null),
                (list) -> list
                        .stream()
                        .filter(Objects::nonNull)
                        .min(comparator)
                        .orElse(null));
        if (result == null) {
            throw new NoSuchElementException("no minimum");
        }
        return result;
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFunc(
                threads,
                values,
                (list) -> list
                        .stream()
                        .allMatch(predicate),
                (list) -> list
                        .stream()
                        .allMatch(e -> e));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
