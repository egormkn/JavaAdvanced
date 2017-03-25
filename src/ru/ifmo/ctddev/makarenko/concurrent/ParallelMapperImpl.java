package ru.ifmo.ctddev.makarenko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        return null;
    }

    @Override
    public void close() throws InterruptedException {

    }
}
