package ru.ifmo.ctddev.makarenko.concurrent;

import info.kgeorgiy.java.advanced.base.BaseTester;
import info.kgeorgiy.java.advanced.concurrent.ListIPTest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Tester extends BaseTester {
    public Tester() {
    }

    public static void main(String[] var0) throws NoSuchAlgorithmException, IOException {
        (new Tester()).add("scalar", ScalarIPTest.class).add("list", ListIPTest.class).run(var0);
    }
}