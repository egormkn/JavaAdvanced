package ru.ifmo.ctddev.makarenko.walk;

import java.io.*;
import java.nio.file.*;

public class RecursiveWalk {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java RecursiveWalk <input file> <output file>");
            return;
        }

        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String s;
            while ((s = reader.readLine()) != null) {
                Path path = Paths.get(s);
                if (Files.isDirectory(path)) {
                    Files.walkFileTree(path, new HashVisitor(writer));
                } else {
                    writer.append(Walk.hash(path)).append(' ').append(s).append(System.lineSeparator());
                }
            }
        } catch (NoSuchFileException e) {
            System.out.println("Input file '" + inputFile + "' does not exists");
        } catch (SecurityException e) {
            System.out.println("Input file '" + inputFile + "' is protected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
