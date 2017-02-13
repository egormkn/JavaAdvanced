package ru.ifmo.ctddev.makarenko.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {

    public static void main(String[] args) {
        if (args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Walk <input file> <output file>");
            return;
        }

        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
                String s;
                while ((s = reader.readLine()) != null) {
                    try {
                        writer
                                .append(hash(Paths.get(s)))
                                .append(' ')
                                .append(s)
                                .append(System.lineSeparator());
                    } catch (IOException e) {
                        System.err.println("Output error: " + e.getMessage());
                    }
                }
            } catch (NoSuchFileException e) {
                System.err.println("Input file '" + inputFile + "' does not exists");
            } catch (SecurityException e) {
                System.err.println("Input file '" + inputFile + "' security violation");
            } catch (IOException e) {
                System.err.println("Input error: " + e.getMessage());
            }
        } catch (NoSuchFileException e) {
            System.err.println("Output file '" + outputFile + "' does not exists");
        } catch (SecurityException e) {
            System.err.println("Output file '" + outputFile + "' security violation");
        } catch (IOException e) {
            System.err.println("Output error: " + e.getMessage());
        }
    }

    private static String hash(Path path) {
        if (Files.isDirectory(path)) {
            return Utils.DEFAULT_HASH;
        } else {
            return Utils.hash(path);
        }
    }
}
