package ru.ifmo.ctddev.makarenko.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ParallelMapper} interface that
 * allows to run parallel tasks on {@link List} elements.
 *
 * @author Egor Makarenko
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> queue;
    private final Thread[] threads;

    /**
     * Creates a new instance of {@link ParallelMapper}.
     *
     * @param threads number of threads to use in {@link #map(Function, List)}
     */
    public ParallelMapperImpl(int threads) {
        this.queue = new ArrayDeque<>();
        this.threads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            this.threads[i] = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (queue) {
                            while (queue.isEmpty()) {
                                queue.wait();
                            }
                            task = queue.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            this.threads[i].start();
        }
    }

    /**
     * Applies {@link Function} to each element of given {@link List}
     * and returns a list of results.
     *
     * @param function function to apply
     * @param list     list of arguments
     * @param <T>      type of list elements
     * @param <R>      type of result
     * @return new list of results of applying {@code function} to {@code list}
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Task<T, R>> tasks = new ArrayList<>(list.size());
        for (T element : list) {
            tasks.add(new Task<>(function, element));
        }
        synchronized (queue) {
            queue.addAll(tasks);
            queue.notifyAll();
        }

        for (Task<T, R> task : tasks) {
            task.waitResult();
        }

        return tasks.stream().map(Task::getResult).collect(Collectors.toList());
    }

    /**
     * Closes all threads and joins them to the current thread.
     *
     * @throws InterruptedException if some of created threads was interrupted
     */
    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private class Task<T, R> implements Runnable {

        private final Function<? super T, ? extends R> function;
        private final T element;
        private R result;
        private boolean ready = false;

        Task(Function<? super T, ? extends R> function, T element) {
            this.function = function;
            this.element = element;
            this.result = null;
            this.ready = false;
        }

        @Override
        public synchronized void run() {
            if (!ready) {
                result = function.apply(element);
                ready = true;
                notify();
            }
        }

        synchronized R getResult() {
            return result;
        }

        synchronized void waitResult() throws InterruptedException {
            while (!ready) {
                wait();
            }
        }
    }
}
