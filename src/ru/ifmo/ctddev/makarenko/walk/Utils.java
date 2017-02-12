package ru.ifmo.ctddev.makarenko.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class Utils {

    public static final String defaultHash = "00000000";

    public static String hash(Path path) {
        assert !Files.isDirectory(path);
        String hash = defaultHash;
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

    private Utils() {}
}
