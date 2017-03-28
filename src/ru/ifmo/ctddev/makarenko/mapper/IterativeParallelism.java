package ru.ifmo.ctddev.makarenko.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Math.min;

/**
 * Implementation of interface {@link ListIP}, that provides
 * concurrent methods for {@link List}.
 *
 * @author Egor Makarenko
 */
public class IterativeParallelism implements ListIP {

    private final ParallelMapper mapper;

    /**
     * Create an instance of {@link IterativeParallelism}
     * that generates threads for parallel list processing.
     */
    public IterativeParallelism() {
        this(null);
    }

    /**
     * Create an instance of {@link IterativeParallelism}
     * that uses {@link ParallelMapper} for parallel list processing.
     *
     * @param mapper ParallelMapper to use
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns the maximum element of {@link List} according to
     * the provided {@link Comparator}.
     *
     * @param threads    number of threads to use
     * @param list       list to process
     * @param comparator comparator to use
     * @return maximum element of {@link List} or {@code null} if list is empty
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        if (list.isEmpty()) {
            return null;
        }
        Function<List<? extends T>, T> max = l -> Collections.max(l, comparator);
        return parallelize(threads, list, max, max);
    }

    /**
     * Returns the minimum element of {@link List} according to
     * the provided {@link Comparator}.
     *
     * @param threads    number of threads to use
     * @param list       list to process
     * @param comparator comparator to use
     * @return minimum element of {@link List} or {@code null} if list is empty
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    /**
     * Returns whether all elements of {@link List} match the provided {@link Predicate}.
     * May not evaluate the predicate on all elements if not necessary
     * for determining the result. If the list is empty then {@code true}
     * is returned and the predicate is not evaluated.
     *
     * @param threads   number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return {@code true} if either all elements of the {@code list} match the
     * provided predicate or the {@code list} is empty, otherwise {@code false}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, list, predicate.negate());
    }

    /**
     * Returns whether any elements of {@link List} match the provided {@link Predicate}.
     * May not evaluate the predicate on all elements if not necessary
     * for determining the result. If the {@code list} is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * @param threads   number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return {@code true} if any elements of the {@code list} match the
     * provided predicate, otherwise {@code false}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> match = l -> l.stream().anyMatch(predicate);
        Function<List<? extends Boolean>, Boolean> matchList = r -> r.stream().anyMatch(y -> y);
        return parallelize(threads, list, match, matchList);
    }

    /**
     * Returns concatenated string representation of all elements from {@link List}.
     * Returns empty string if {@code list} is empty.
     *
     * @param threads number of threads to use
     * @param list    list to process
     * @return concatenation of string representations of all {@code list} elements
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        Function<List<?>, String> joinObjects = l -> l.stream().map(Object::toString).collect(Collectors.joining());
        Function<List<? extends String>, String> joinStrings = s -> s.stream().collect(Collectors.joining());
        return parallelize(threads, list, joinObjects, joinStrings);
    }

    /**
     * Returns a {@link List} consisting of the elements of this list that match
     * the given {@link Predicate}.
     *
     * @param threads   number of threads to use
     * @param list      list to process
     * @param predicate predicate to apply to elements
     * @return the new list
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
     *
     * @param threads  number of threads to use
     * @param list     list to process
     * @param function function to apply to elements
     * @return the new list
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<List<? extends T>, List<U>> map = l -> l.stream().map(function).collect(Collectors.toList());
        Function<List<? extends List<U>>, List<U>> mapLists = l -> l.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelize(threads, list, map, mapLists);
    }

    private <T, R> R parallelize(int threads, List<? extends T> list,
                                 final Function<List<? extends T>, R> threadJob,
                                 final Function<List<? extends R>, R> finalJob) throws InterruptedException {

        int size = (list.size() + threads - 1) / threads;
        List<List<? extends T>> subLists = new ArrayList<>(threads);
        for (int start = 0; start < list.size(); start += size) {
            int end = min(list.size(), start + size);
            subLists.add(list.subList(start, end));
        }

        List<R> result;

        if (mapper == null) {
            result = new ArrayList<>(Collections.nCopies(subLists.size(), null));
            List<Thread> pool = new ArrayList<>(threads);
            for (int i = 0; i < subLists.size(); i++) {
                final int position = i;
                Thread t = new Thread(
                        () -> result.set(position, threadJob.apply(subLists.get(position)))
                );
                t.start();
                pool.add(t);
            }
            for (Thread t : pool) {
                t.join();
            }
        } else {
            result = mapper.map(threadJob, subLists);
        }

        return finalJob.apply(result);
    }
}
