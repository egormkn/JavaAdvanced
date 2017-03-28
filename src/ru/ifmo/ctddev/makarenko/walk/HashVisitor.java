package ru.ifmo.ctddev.makarenko.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashVisitor extends SimpleFileVisitor<Path> {

    private final Writer writer;

    public HashVisitor(Writer writer) {
        this.writer = writer;
    }

    private void printHash(Path file, String hash) throws IOException {
        writer.append(hash).append(' ').append(file.toString()).append(System.lineSeparator());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        printHash(file, Utils.hash(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printHash(file, Utils.DEFAULT_HASH);
        return FileVisitResult.CONTINUE;
    }
}
