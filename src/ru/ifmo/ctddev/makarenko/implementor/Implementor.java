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

/**
 * Generates implementation of class or interface.
 *
 * @author Egor Makarenko
 * @version 1.1
 */

public class Implementor implements JarImpler {

    /**
     * Path separator used in zip archives
     */
    public static final String ZIP_ENTRY_SEPARATOR = "/";

    /**
     * Suffix of generated sources and classes
     */
    public static final String IMPL_SUFFIX = "Impl";

    /**
     * Temporary directory for compiled <tt>*.class</tt> files
     */
    public static final String TEMP_DIR = "./temp";

    /**
     * Implements java class and optionally builds jar artifact
     * with implemented class.
     * <br>
     * <p>
     * Usage:
     * <br>
     * <code>java -jar Implementor.jar &lt;classname&gt;</code>
     * <br>
     * or
     * <br>
     * <code>java -jar Implementor.jar -jar &lt;classname&gt; &lt;file.jar&gt;</code>
     * </p>
     *
     * @param args program arguments
     */
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

    /**
     * Produces code implementing class or interface specified
     * by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name
     * of the type token with <tt>{@value #IMPL_SUFFIX}</tt> suffix
     * added. Generated source code should have correct file name
     * and be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory corresponding to class package name.
     * For example, the implementation of the interface {@link java.util.List}
     * should go to <tt>$root/java/util/ListImpl.java</tt>
     * </p>
     *
     * @param token type token to create implementation for
     * @param root root directory
     * @throws ImplerException when implementation cannot be generated
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Package pack = token != null ? token.getPackage() : null;
        if (pack != null) {
            root = root.resolve(pack.getName().replace(".", File.separator));
        }
        implementSrc(token, root);
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface
     * specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name
     * of the type token with <tt>{@value #IMPL_SUFFIX}</tt> added.
     * </p>
     *
     * @param token type token to create implementation for
     * @param jarFile target <tt>.jar</tt> file
     * @throws ImplerException when implementation cannot be generated
     */
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
                entryName = token.getPackage().getName().replace(".", ZIP_ENTRY_SEPARATOR)
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

        } catch (Exception e) {
            throw new ImplerException(e);
        } finally {
            try {
                Files.deleteIfExists(source);
                Files.deleteIfExists(impl);
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {}
        }
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>{@value #IMPL_SUFFIX}</tt> suffix
     * added. Generated source code should be placed in the <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/ListImpl.java</tt>
     * </p>
     *
     * @param token type token to create implementation for
     * @param root root directory
     * @throws ImplerException when implementation cannot be generated
     */
    private void implementSrc(Class<?> token, Path root) throws ImplerException {
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

        try (BufferedWriter writer = Files.newBufferedWriter(Files.createDirectories(root).resolve(filename))) {
            new ClassWriter(writer).print(token);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Generates string representing source file name
     * of class implementation.
     * <p>
     * Source file name should be same as full name of
     * the type token with <tt>{@value #IMPL_SUFFIX}.java</tt> added.
     * For example, source file name for interface {@link java.util.List}
     * should be <tt>ListImpl.java</tt>
     * </p>
     *
     * @param token type token
     * @return source file name of class implementation
     */
    private String getSourceName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX + ".java";
    }


    /**
     * Generates string representing compiled class file name
     * of class implementation.
     * <p>
     * Class file name should be same as full name of the type token
     * with <tt>{@value #IMPL_SUFFIX}.class</tt> added.
     * For example, source file name for interface {@link java.util.List}
     * should be <tt>ListImpl.java</tt>
     * </p>
     *
     * @param token type token
     * @return compiled class file name of class implementation
     */
    private String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX + ".class";
    }

    /**
     * Compiles java files to classes. Generated <tt>*.class</tt>
     * files will be placed to the <tt>root</tt> directory
     *
     * @param files {@link List} of files that should be compiled
     * @param root directory to store compiled <tt>*.class</tt> files
     * @throws Exception when compiler tools were not found
     *         or compiler finished with non-zero return code
     */
    public static void compileFiles(final List<String> files, final Path root) throws Exception {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new Exception("Could not find java compiler, include tools.jar to classpath");
        }
        final List<String> args = new ArrayList<>();
        args.addAll(files);
        args.add("-cp");
        args.add(root + File.pathSeparator + System.getProperty("java.class.path"));
        final int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) {
            throw new Exception("Compiler exit code: " + exitCode);
        }
    }
}
