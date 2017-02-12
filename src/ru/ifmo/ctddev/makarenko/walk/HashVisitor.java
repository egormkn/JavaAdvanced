package ru.ifmo.ctddev.makarenko.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class HashVisitor implements FileVisitor<Path> {
    private final BufferedWriter writer;

    public HashVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        writer.append(Walk.hash(file)).append(' ').append(file.toString()).append(System.lineSeparator());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        writer.append("00000000").append(' ').append(file.toString()).append(System.lineSeparator());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
