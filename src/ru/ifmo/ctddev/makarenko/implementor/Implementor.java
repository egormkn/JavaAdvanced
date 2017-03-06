package ru.ifmo.ctddev.makarenko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class Implementor implements JarImpler {

    public static final String PACKAGE_SEPARATOR = ".";
    public static final String ZIP_ENTRY_SEPARATOR = "/";
    public static final String IMPL_SUFFIX = "Impl";
    public static final String IMPL_EXTENSION = ".java";
    public static final String CLASS_EXTENSION = ".class";
    public static final String TEMP_DIR = "./temp";

    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 3)
                || (args.length == 1 && args[0] == null)
                || (args.length == 3 && (!"-jar".equals(args[0]) || args[1] == null || args[2] == null))) {
            System.err.println("Usage: java Implementor <classname>");
            System.err.println("       java Implementor -jar <classname> <filename>");
            return;
        }

        try {
            if (args.length == 3) {
                new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                new Implementor().implement(Class.forName(args[0]), Paths.get(TEMP_DIR));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class/interface not found: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Class/interface cannot be implemented: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Package pack = token != null ? token.getPackage() : null;
        if (pack != null) {
            root = root.resolve(pack.getName().replace(PACKAGE_SEPARATOR, File.separator));
        }
        implementSrc(token, root);
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path temp = Paths.get(TEMP_DIR);
        Path source = temp.resolve(getSourceName(token));
        Path impl = temp.resolve(getClassName(token));

        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, token.getName() + IMPL_SUFFIX);
        attrs.put(new Attributes.Name("Created-By"), this.getClass().getPackage().getName());

        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            implementSrc(token, temp);
            compileFiles(Collections.singletonList(source.toString()), temp);

            String entryName = impl.getFileName().toString();
            Package pack = token.getPackage();
            if (pack != null) {
                entryName = token.getPackage().getName().replace(PACKAGE_SEPARATOR, ZIP_ENTRY_SEPARATOR)
                        + ZIP_ENTRY_SEPARATOR + entryName;
            }

            JarEntry entry = new JarEntry(entryName);
            out.putNextEntry(entry);

            try (InputStream is = new BufferedInputStream(Files.newInputStream(impl))) {
                int len;
                byte buffer[] = new byte[1024];
                while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

        } catch (IOException e) {
            throw new ImplerException(e);
        } finally {
            /*try {
                Files.deleteIfExists(source);
                Files.deleteIfExists(impl);
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {}*/
        }
    }

    private void implementSrc(Class<?> token, Path folder) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token can't be null");
        } else if (token.isPrimitive()) {
            throw new ImplerException("Unsupported token type: primitive type");
        } else if (token.isArray()) {
            throw new ImplerException("Unsupported token type: array");
        } else if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Token is final");
        } else if (token.isEnum() || token.equals(Enum.class)) {
            throw new ImplerException("Token is enum");
        }

        Constructor[] constructors = token.getDeclaredConstructors();
        Predicate<Constructor> notPrivate = c -> !Modifier.isPrivate(c.getModifiers());
        if (constructors.length > 0 && Stream.of(constructors).filter(notPrivate).count() == 0) {
            throw new ImplerException("There is no public/protected constructors");
        }

        String filename = getSourceName(token);

        try (BufferedWriter writer = Files.newBufferedWriter(Files.createDirectories(folder).resolve(filename))) {
            new ClassWriter(writer).print(token);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    private String getSourceName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX + IMPL_EXTENSION;
    }

    private String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX + CLASS_EXTENSION;
    }

    public static void compileFiles(final List<String> files, final Path root) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IOException("Could not find java compiler, include tools.jar to classpath");
        }
        final List<String> args = new ArrayList<>();
        args.addAll(files);
        args.add("-cp");
        args.add(root + File.pathSeparator + System.getProperty("java.class.path"));
        final int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) {
            throw new IOException("Compiler exit code: " + exitCode);
        }
    }
}
