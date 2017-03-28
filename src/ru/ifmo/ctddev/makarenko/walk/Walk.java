package ru.ifmo.ctddev.makarenko.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;

import static ru.ifmo.ctddev.makarenko.walk.Utils.getPath;

public class Walk {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Walk <input file> <output file>");
            return;
        }

        Path inputFile = getPath(args[0]);
        Path outputFile = getPath(args[1]);

        if (inputFile == null) {
            System.err.println("Invalid path to input file: '" + args[0] + "'");
            return;
        }

        if (outputFile == null) {
            System.err.println("Invalid path to output file: '" + args[1] + "'");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
                String s;
                while ((s = reader.readLine()) != null) {
                    printHashes(writer, s);
                }
            } catch (NoSuchFileException e) {
                System.err.println("Input file '" + inputFile + "' does not exists");
            } catch (SecurityException e) {
                System.err.println("Input file '" + inputFile + "' security violation");
            } catch (IOException e) {
                System.err.println("Input error: " + e.getMessage());
            }
        } catch (SecurityException e) {
            System.err.println("Output file '" + outputFile + "' security violation");
        } catch (IOException e) {
            System.err.println("Output error: " + e.getMessage());
        }
    }

    private static String hash(Path path) {
        return path == null || Files.isDirectory(path) ? Utils.DEFAULT_HASH : Utils.hash(path);
    }

    protected static void printHashes(Writer writer, String s) throws IOException {
        Path path = getPath(s);
        if (path == null) {
            System.err.println("Invalid path to file: '" + s + "'");
            writer
                    .append(Utils.DEFAULT_HASH)
                    .append(' ')
                    .append(s)
                    .append(System.lineSeparator());
            return;
        }
        try {
            writer
                    .append(hash(path))
                    .append(' ')
                    .append(s)
                    .append(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Output error: " + e.getMessage());
        }
    }
}
