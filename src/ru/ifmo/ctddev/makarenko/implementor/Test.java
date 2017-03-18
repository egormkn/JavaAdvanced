package ru.ifmo.ctddev.makarenko.implementor;

import java.io.IOException;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        try {
            new ClassWriter(System.out).print(ArrayList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}