package ru.ifmo.ctddev.makarenko.implementor;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static ru.ifmo.ctddev.makarenko.implementor.Implementor.IMPL_SUFFIX;

/**
 * Writes class implementation to {@link ClassWriter#output}
 */
public class ClassWriter {

    /**
     * Default line separator
     */
    private static final String LINE = System.lineSeparator();

    /**
     * Tab string
     */
    private static final String TAB = "    ";

    /**
     * Output stream
     */
    private final Appendable output;

    /**
     * Map of methods that should be implemented
     */
    private final Map<String, Method> methods;

    /**
     * Map of generic types
     */
    private final Map<Type, Type> generics;

    /**
     * Public constructor
     *
     * @param output Output stream
     */
    public ClassWriter(@NotNull Appendable output) {
        this.output = output;
        this.methods = new TreeMap<>();
        this.generics = new HashMap<>();
    }

    /**
     * Get default value by type token
     * @param type type token
     * @return default type value as stated in
     *         <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">
     *              documentation
     *         </a>
     */
    private String getDefaultValue(@NotNull Class<?> type) {
        if (type.equals(void.class)) {
            return "";
        } else if (type.equals(byte.class) || type.equals(short.class) || type.equals(int.class)) {
            return "0";
        } else if (type.equals(long.class)) {
            return "0L";
        } else if (type.equals(float.class)) {
            return "0.0f";
        } else if (type.equals(double.class)) {
            return "0.0d";
        } else if (type.equals(char.class)) {
            return "'\\u0000'";
        } else if (type.equals(boolean.class)) {
            return "false";
        } else {
            return "null";
        }
    }

    /**
     * Get modifiers by mask
     *
     * @param modifiers bit mask
     * @return string of modifiers except <tt>abstract</tt>
     */
    private String getModifiers(int modifiers) {
        modifiers &= ~Modifier.ABSTRACT;
        return modifiers == 0 ? "" : Modifier.toString(modifiers);
    }

    /**
     * Get exceptions string
     *
     * @param exceptions array of exceptions types
     * @return string of exception types separated by comma
     * @see ClassWriter#getExceptions(Type[], Map)
     */
    private String getExceptions(@NotNull Type[] exceptions) {
        return getExceptions(exceptions, null);
    }

    /**
     * Get generic exceptions string
     *
     * @param exceptions array of exceptions types
     * @param generics map of generic types that should be replaced
     * @return string of exception types separated by comma
     */
    private String getExceptions(@NotNull Type[] exceptions, @Nullable Map<Type, Type> generics) {
        if (exceptions.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" throws ");
        for (int i = 0; i < exceptions.length; i++) {
            if (exceptions[i] instanceof Class) {
                sb.append(((Class) exceptions[i]).getName());
            } else {
                String exceptName = exceptions[i].toString();
                if (generics != null) {
                    Type replace = generics.get(exceptions[i]);
                    if (replace != null) {
                        exceptName = replace.getTypeName();
                    }
                }
                sb.append(exceptName);
            }
            if (i < exceptions.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Get type parameters string
     *
     * @param types array of {@link TypeVariable} representing generic types
     * @param withBounds whether lower and upper bounds should be included
     * @return string of type parameters
     */
    private String getTypeParameters(TypeVariable<?>[] types, boolean withBounds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            sb.append(i == 0 ? '<' : ", ");
            sb.append(types[i].getTypeName());
            // generics.add(types[i]);
            Type[] bounds = types[i].getBounds();
            if (withBounds && (bounds.length != 1 || !bounds[0].getTypeName().equals(Object.class.getTypeName()))) {
                for (int j = 0; j < bounds.length; j++) {
                    if (j == 0) {
                        sb.append(" extends ");
                    } else {
                        sb.append(" & ");
                    }
                    sb.append(bounds[j].getTypeName());
                }
            }
            if (i == types.length - 1) {
                sb.append('>');
            }
        }
        return sb.toString();
    }

    /**
     * Get {@link Executable} arguments
     *
     * @param exec executable
     * @param onlyNames whether argument types should be included
     * @return string of {@link Executable} arguments
     */
    private String getArguments(Executable exec, boolean onlyNames) {
        StringBuilder sb = new StringBuilder("(");
        Type[] params = exec.getGenericParameterTypes();
        for (int j = 0; j < params.length; j++) {
            String param = params[j].getTypeName();
            if (exec.isVarArgs() && (j == params.length - 1)) {
                param = param.replaceFirst("\\[\\]$", "..."); // replace T[] with T...
            }

            if (!onlyNames) sb.append(param);
            sb.append(" arg");
            sb.append(j);
            if (j < (params.length - 1)) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Prints class or interface implementation to the {@link ClassWriter#output}
     * @param token type token of class to be implemented
     * @throws IOException when something goes wrong during the output
     */
    public void print(Class<?> token) throws IOException {
        if (token == null) {
            return;
        }
        Package pack = token.getPackage();
        if (pack != null) {
            output.append("package ").append(pack.getName()).append(';');
            output.append(LINE).append(LINE);
        }
        output.append(toGenericString(token));
        output.append(token.isInterface() ? " implements " : " extends ");
        output.append(token.getSimpleName());
        output.append(getTypeParameters(token.getTypeParameters(), false));
        output.append(" {").append(LINE).append(LINE);

        printConstructors(token);
        printMethods(token, token.getTypeParameters());

        output.append("}").append(LINE);

        /*for (Type t : generics) {
            System.err.println(t.getTypeName());
        }*/
    }

    /**
     * Print constructors of class implementation to the {@link ClassWriter#output}
     *
     * @param token type token
     * @throws IOException when something goes wrong during the output
     */
    private void printConstructors(Class<?> token) throws IOException {
        if (token == null) {
            return;
        }

        for (Constructor constructor : token.getDeclaredConstructors()) {
            output.append(TAB).append(toGenericString(constructor)).append(" {").append(LINE);
            output.append(TAB).append(TAB).append("super").append(getArguments(constructor, true)).append(';').append(LINE);
            output.append(TAB).append("}").append(LINE).append(LINE);
        }
    }

    /**
     * Add method to {@link ClassWriter#methods} map
     *
     * @param method method to add
     */
    private void addMethod(Method method) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return;
        }
        if (Modifier.isFinal(method.getModifiers())) {
            return;
        }
        String name = method.getName() + getArguments(method, false);

        Method other = methods.get(name);
        if (other == null) {
            methods.put(name, method);
        } else {
            Class<?> old = other.getReturnType();
            Class<?> newType = method.getReturnType();
            if (old.isAssignableFrom(newType)) {
                methods.put(name, method);
            }
        }
    }

    /**
     * Recursively add to {@link ClassWriter#methods} parent methods that should be implemented
     *
     * @param token type token
     */
    private void addParentMethods(Class<?> token) {
        for (Method method : token.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                continue; // Was added in getMethods()
            }
            if (token.isInterface() || Modifier.isAbstract(method.getModifiers())) {
                addMethod(method);
            }
        }
        Class<?> superClass = token.getSuperclass();
        if (superClass != null
                && Modifier.isAbstract(token.getModifiers())
                && Modifier.isAbstract(superClass.getModifiers())) {
            addParentMethods(superClass);
        }
    }

    /**
     * Print methods of class implementation to the {@link ClassWriter#output}
     *
     * @param token type token
     * @param types generic types
     * @throws IOException when something goes wrong during the output
     */
    private void printMethods(Class<?> token, TypeVariable<? extends Class<?>>[] types) throws IOException {
        for (Method method : token.getMethods()) {
            if (token.isInterface() || Modifier.isAbstract(method.getModifiers())) {
                addMethod(method);
            }
        }
        addParentMethods(token);

        for (Method method : methods.values()) {
            if (method.isDefault()) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            for (Annotation annotation : method.getAnnotations()) {
                output.append(TAB).append(annotation.toString()).append(LINE);
            }
            output.append(TAB).append(toGenericString(method));

            if (Modifier.isNative(method.getModifiers())) {
                output.append(';');
            } else {
                output.append(" {").append(LINE);
                output.append(TAB).append(TAB).append("return");
                String defaultValue = getDefaultValue(method.getReturnType());
                if (defaultValue.length() > 0) {
                    output.append(' ');
                }
                output.append(defaultValue).append(";").append(LINE);
                output.append(TAB).append("}");
            }
            output.append(LINE).append(LINE);
        }
    }

    /**
     * Get string of class declaration by type token
     *
     * @param c type token
     * @return string of class declaration
     */
    private String toGenericString(Class<?> c) {
        return getModifiers(c.getModifiers() & Modifier.classModifiers()) + ' ' +
                " class " +
                c.getSimpleName() +
                IMPL_SUFFIX +
                getTypeParameters(c.getTypeParameters(), true);
    }

    /**
     * Get string of method declaration
     *
     * @param method method
     * @return string of method declaration
     */
    private String toGenericString(Method method) {
        return getModifiers(method.getModifiers() & Modifier.methodModifiers()) + ' ' +
                getTypeParameters(method.getTypeParameters(), true) + ' ' +
                method.getGenericReturnType().getTypeName() + ' ' +
                method.getName() +
                getArguments(method, false) +
                getExceptions(method.getGenericExceptionTypes());
    }

    /**
     * Get string of constructor declaration
     *
     * @param constructor constructor
     * @return string of constructor declaration
     */
    private String toGenericString(Constructor constructor) {
        return getModifiers(constructor.getModifiers() & Modifier.constructorModifiers()) + ' ' +
                constructor.getDeclaringClass().getSimpleName() + IMPL_SUFFIX +
                getTypeParameters(constructor.getTypeParameters(), true) +
                getArguments(constructor, false) +
                getExceptions(constructor.getGenericExceptionTypes());
    }
}