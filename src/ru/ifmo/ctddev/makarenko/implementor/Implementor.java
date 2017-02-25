package ru.ifmo.ctddev.makarenko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    public static final String PACKAGE_SEPARATOR = ".";
    public static final String IMPL_SUFFIX = "Impl";
    public static final String IMPL_EXTENSION = ".java";

    private static boolean debug = false;

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args[0] == null) {
            System.err.println("Usage: java Implementor <classname>");
            return;
        }
        debug = true;
        try {
            Class c = Class.forName(args[0]);
            new Implementor().implement(c, Paths.get("./test00_default"));
        } catch (ClassNotFoundException e) {
            System.err.println("Class/interface not found: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Class/interface cannot be implemented: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token can't be null");
        } else if (token.isPrimitive()) {
            throw new ImplerException("Unsupported token type: primitive type");
        } else if (token.isArray()) {
            throw new ImplerException("Unsupported token type: array");
        } else if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Token is final");
        }

        Package pack = token.getPackage();
        if (pack != null) {
            path = path.resolve(pack.getName().replace(PACKAGE_SEPARATOR, File.separator));
        }
        String filename = token.getSimpleName() + IMPL_SUFFIX + IMPL_EXTENSION;

        try (BufferedWriter writer = Files.newBufferedWriter(Files.createDirectories(path).resolve(filename))) {
            new ClassWriter(debug ? System.out : writer).print(token);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }
}
