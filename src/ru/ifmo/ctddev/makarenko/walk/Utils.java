package ru.ifmo.ctddev.makarenko.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class Utils {

    public static final String DEFAULT_HASH = "00000000";

    public static final int INITIAL_HASH = 0x811c9dc5;
    public static final int PRIME_NUMBER = 0x01000193;

    public static String hash(Path path) {
        // assert !Files.isDirectory(path);
        String hash = DEFAULT_HASH;
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int h = INITIAL_HASH;
            while((bytesRead = stream.read(buffer)) != -1){
                for (int i = 0; i < bytesRead; i++) {
                    h = (h * PRIME_NUMBER) ^ (buffer[i] & 0xff);
                }
            }
            hash = String.format("%08x", h);
        } catch (NoSuchFileException e) {
            System.err.println("File '" + path + "' not found");
        } catch (AccessDeniedException e) {
            System.err.println("File '" + path + "' security violation");
        } catch (IOException e) {
            System.err.println("I/O error for '" + path + "':" + e.getMessage());
        }
        return hash;
    }

    private Utils() {}
}
