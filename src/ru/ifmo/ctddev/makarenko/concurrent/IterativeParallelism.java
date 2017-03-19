package ru.ifmo.ctddev.makarenko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class IterativeParallelism implements ListIP {

    private <T, R> R parallelise(int threads, List<? extends T> list,
                                 final Function<List<? extends T>, R> threadJob,
                                 final Function<List<? extends R>, R> finalJob) throws InterruptedException {
        List<Thread> pool = new ArrayList<>(threads);
        List<R> result = new ArrayList<>(threads);

        int len = (list.size() + threads - 1) / threads;

        for (int i = 0; i < list.size(); i += len) {
            final int start = i;
            final int end = min(list.size(), i + len);
            final int position = result.size();
            result.add(null);
            Thread t = new Thread(
                    () -> result.set(position, threadJob.apply(list.subList(start, end)))
            );
            t.start();
            pool.add(t);
        }
        for (Thread t : pool) {
            t.join();
        }

        return finalJob.apply(result);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = l -> Collections.max(l, comparator);
        return parallelise(threads, list, max, max);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, list, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> match = l -> l.stream().anyMatch(predicate);
        Function<List<? extends Boolean>, Boolean> matchList = r -> r.stream().anyMatch(y -> y);
        return parallelise(threads, list, match, matchList);
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        Function<List<?>, String> joinObjects = l -> l.stream().map(Object::toString).collect(Collectors.joining());
        Function<List<? extends String>, String> joinStrings = s -> s.stream().collect(Collectors.joining());
        return parallelise(threads, list, joinObjects, joinStrings);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filter = l -> l.stream().filter(predicate).collect(Collectors.toList());
        Function<List<? extends List<T>>, List<T>> filterLists = l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelise(threads, list, filter, filterLists);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<List<? extends T>, List<U>> map = l -> l.stream().map(function).collect(Collectors.toList());
        Function<List<? extends List<U>>, List<U>> mapLists = l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelise(threads, list, map, mapLists);
    }
}
