package ru.ifmo.ctddev.makarenko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    public static final String PACKAGE_SEPARATOR = ".";
    public static final String IMPL_SUFFIX = "Impl";
    public static final String SOURCE_EXTENSION = ".java";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Implementor <classname>");
            return;
        }

        try {
            Class c = Class.forName(args[0]);
            new Implementor().implement(c, Paths.get("./test00"));
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found!");
        } catch (ImplerException e) {
            System.err.println("ImplerException: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        if (token.isArray() || token.isPrimitive()) {
            throw new ImplerException("Unsupported token type");
        }

        Package pack = token.getPackage();
        if (pack != null) {
            path = path.resolve(pack.getName().replace(PACKAGE_SEPARATOR, File.separator));
        }
        String filename = token.getSimpleName() + IMPL_SUFFIX + SOURCE_EXTENSION;

        System.out.println("Path: " + path.toString());
        System.out.println("Filename: " + filename);

        try (BufferedWriter writer = Files.newBufferedWriter(Files.createDirectories(path).resolve(filename))) {
            new ClassPrinter(token, System.out).print();
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }
}
