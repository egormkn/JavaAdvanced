package ru.ifmo.ctddev.makarenko.concurrent;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScalarIPTest extends info.kgeorgiy.java.advanced.concurrent.ScalarIPTest {

    private final Random random = new Random(3257083275083275083L);

    protected List<Integer> randomList(int var1) {
        int[] var2 = this.random.ints((long)Math.min(var1, 1000000)).toArray();
        return IntStream.generate(() -> var2[this.random.nextInt(var2.length)])
                .boxed().limit(var1).collect(Collectors.toList());
    }
}