package ru.ifmo.ctddev.makarenko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Math.min;

/**
 * Implementation of interface {@link ListIP}, that provides
 * concurrent methods for {@link List}
 *
 * @author Egor Makarenko
 */
public class IterativeParallelism implements ListIP {

    private <T, R> R parallelize(int threads, List<? extends T> list,
                                 final Function<List<? extends T>, R> threadJob,
                                 final Function<List<? extends R>, R> finalJob) throws InterruptedException {
        List<Thread> pool = new ArrayList<>(threads);
        List<R> result = new ArrayList<>(threads);

        int size = (list.size() + threads - 1) / threads;

        for (int i = 0; i < list.size(); i += size) {
            final int start = i;
            final int end = min(list.size(), i + size);
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

    /**
     * Returns the maximum element of {@code List} according to the provided {@link java.util.Comparator}.
     * Uses {@code count} threads to do this parallel.
     *
     * @param threads      number of threads to use
     * @param list       list to process
     * @param comparator comparator to use
     * @return maximum element of {@code List}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = l -> Collections.max(l, comparator);
        return parallelize(threads, list, max, max);
    }

    /**
     * Returns the minimum element of {@code List} according to the provided {@link java.util.Comparator}.
     * Uses {@code count} threads to do this parallel.
     *
     * @param threads      number of threads to use
     * @param list       list to process
     * @param comparator comparator to use
     * @return minimum element of {@code List}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    /**
     * Returns whether any elements of {@code List} match the provided {@link java.util.function.Predicate}.
     * Returns {@code false} if {@code List} is empty. Uses {@code count} threads to do
     * this parallel.
     *
     * @param threads     number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return {@code true} if any elements of {@code List} match the provided
     * predicate, otherwise {@code false}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, list, predicate.negate());
    }

    /**
     * Returns whether all elements of {@code List} match the provided {@link java.util.function.Predicate}.
     * Returns {@code true} if {@code List} is empty. Uses {@code count} threads
     * to do this parallel.
     *
     * @param threads     number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return {@code true} if either all elements of {@code List} match the
     * provided predicate or {@code List} is empty, otherwise {@code false}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> match = l -> l.stream().anyMatch(predicate);
        Function<List<? extends Boolean>, Boolean> matchList = r -> r.stream().anyMatch(y -> y);
        return parallelize(threads, list, match, matchList);
    }

    /**
     * Returns concatenated string representation of all elements from {@code List}.
     * Returns empty string if {@code List} is empty. Uses {@code count} threads to
     * do this parallel.
     *
     * @param threads number of threads to use
     * @param list  list to process
     * @return string, containing concatenated string representations of all elements
     * from {@code List}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        Function<List<?>, String> joinObjects = l -> l.stream().map(Object::toString).collect(Collectors.joining());
        Function<List<? extends String>, String> joinStrings = s -> s.stream().collect(Collectors.joining());
        return parallelize(threads, list, joinObjects, joinStrings);
    }

    /**
     * Returns a {@code List} consisting of the elements of given {@code List} that match
     * the given {@link java.util.function.Predicate}. Uses {@code count} threads to do
     * this parallel.
     *
     * @param threads     number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return the new {@code List}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filter = l -> l.stream().filter(predicate).collect(Collectors.toList());
        Function<List<? extends List<T>>, List<T>> filterLists = l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelize(threads, list, filter, filterLists);
    }

    /**
     * Returns a {@link List} consisting of the results of applying the given
     * {@link Function} to the elements of this {@link List}.
     * Uses {@code count} threads to do this parallel.
     *
     * @param threads    number of threads to use
     * @param list     list to process
     * @param function function to apply to elements
     * @return the new {@code List}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<List<? extends T>, List<U>> map = l -> l.stream().map(function).collect(Collectors.toList());
        Function<List<? extends List<U>>, List<U>> mapLists = l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelize(threads, list, map, mapLists);
    }
}
