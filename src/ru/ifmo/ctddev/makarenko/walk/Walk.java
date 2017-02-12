package ru.ifmo.ctddev.makarenko.walk;

import java.io.*;
import java.nio.file.*;

public class Walk {

    public static String hash(Path path) {
        assert !Files.isDirectory(path);
        String hash = "00000000";
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int h = 0x811c9dc5;
            while((bytesRead = stream.read(buffer)) != -1){
                for (int i = 0; i < bytesRead; i++) {
                    h = (h * 0x01000193) ^ (buffer[i] & 0xff);
                }
            }
            hash = String.format("%08x", h);
        } catch (NoSuchFileException e) {
            System.out.println("File '" + path + "' not found");
        } catch (AccessDeniedException e) {
            System.out.println("File '" + path + "' is protected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Walk <input file> <output file>");
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
                    writer.append("00000000").append(' ').append(s).append(System.lineSeparator());
                } else {
                    writer.append(hash(path)).append(' ').append(s).append(System.lineSeparator());
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
