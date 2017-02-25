package ru.ifmo.ctddev.makarenko.implementor;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

import static ru.ifmo.ctddev.makarenko.implementor.Implementor.IMPL_SUFFIX;

/**
 * @see javax.sql.rowset.CachedRowSet
 */

public class ClassWriter {

    private static final String LINE = System.lineSeparator();
    private static final String TAB = "    ";

    private final Appendable output;

    public ClassWriter(Appendable output) {
        this.output = output;
    }

    @NotNull
    private String getDefaultValue(String type) {
        switch (type) {
            case "void":
                return "";
            case "byte":
            case "short":
            case "int":
                return "0";
            case "long":
                return "0L";
            case "float":
                return "0.0f";
            case "double":
                return "0.0d";
            case "char":
                return "'\\u0000'";
            case "boolean":
                return "false";
            default:
                return "null";
        }
    }

    private String getTypeParameters(TypeVariable<?>[] types, boolean withBounds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            sb.append(i == 0 ? '<' : ", ");
            sb.append(types[i].getTypeName());
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

    public void print(Class<?> token) throws IOException {
        if (token == null) {
            return;
        }
        Package pack = token.getPackage();
        if (pack != null) {
            output.append("package ").append(pack.getName()).append(";");
            output.append(LINE).append(LINE);
        }
        output.append(toGenericString(token));
        output.append(token.isInterface() ? " implements " : " extends ");
        output.append(token.getSimpleName());
        output.append(getTypeParameters(token.getTypeParameters(), false));
        output.append(" {").append(LINE).append(LINE);

        printConstructors(token);
        printMethods(token);

        output.append("}").append(LINE);
    }

    private void printConstructors(Class<?> token) throws IOException {
        if (token == null) {
            return;
        }
        for (Constructor constructor : token.getConstructors()) {
            output.append(TAB).append(toGenericString(constructor)).append(" {").append(LINE);
            output.append(TAB).append(TAB).append("super").append(getArguments(constructor, true)).append(";").append(LINE);
            output.append(TAB).append("}").append(LINE).append(LINE);
        }
    }

    private String toGenericString(Constructor constructor) {
        StringBuilder sb = new StringBuilder();


        int mod = constructor.getModifiers() & Modifier.constructorModifiers() & ~Modifier.ABSTRACT;
        if (mod != 0) {
            sb.append(Modifier.toString(mod)).append(' ');
        }

        sb.append(constructor.getDeclaringClass().getSimpleName()).append(IMPL_SUFFIX);
        sb.append(getTypeParameters(constructor.getTypeParameters(), true));

        sb.append(getArguments(constructor, false));

        Type[] exceptions = constructor.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int k = 0; k < exceptions.length; k++) {
                sb.append((exceptions[k] instanceof Class)?
                        ((Class)exceptions[k]).getName():
                        exceptions[k].toString());
                if (k < (exceptions.length - 1))
                    sb.append(',');
            }
        }
        return sb.toString();
    }

    private void printMethods(Class<?> token) throws IOException {
        for (Method method : token.getMethods()) {
            if (Modifier.isFinal(method.getModifiers())) {
                continue;
            }
            output.append(TAB).append(toGenericString(method));

            if (Modifier.isNative(method.getModifiers())) {
                output.append(';');
            } else {
                output.append(" {").append(LINE);
                output.append(TAB).append(TAB).append("return");
                String defaultValue = getDefaultValue(method.getReturnType().getTypeName());
                if (defaultValue.length() > 0) {
                    output.append(' ');
                }
                output.append(defaultValue).append(";").append(LINE);
                output.append(TAB).append("}");
            }
            output.append(LINE).append(LINE);
        }
    }

    private String toGenericString(Class<?> c) {
        StringBuilder sb = new StringBuilder();
        int modifiers = c.getModifiers() & Modifier.classModifiers() & ~Modifier.ABSTRACT;
        if (modifiers != 0) {
            sb.append(Modifier.toString(modifiers));
            sb.append(' ');
        }
        sb.append("class ");
        sb.append(c.getSimpleName());
        sb.append(IMPL_SUFFIX);
        sb.append(getTypeParameters(c.getTypeParameters(), true));
        return sb.toString();
    }

    private String toGenericString(Method method) {
        StringBuilder sb = new StringBuilder();

        int mod = method.getModifiers() & Modifier.methodModifiers() & ~Modifier.ABSTRACT;
        if (mod != 0) {
            sb.append(Modifier.toString(mod)).append(' ');
        }

        TypeVariable<?>[] typeparms = method.getTypeParameters();
        if (typeparms.length > 0) {
            boolean first = true;
            sb.append('<');
            for (TypeVariable<?> typeparm : typeparms) {
                if (!first)
                    sb.append(',');
                // Class objects can't occur here; no need to test
                // and call Class.getName().
                sb.append(typeparm.toString());
                first = false;
            }
            sb.append("> ");
        }

        Type genRetType = method.getGenericReturnType();
        sb.append(genRetType.getTypeName()).append(' ');
        sb.append(method.getName());

        sb.append(getArguments(method, false));
        Type[] exceptions = method.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int k = 0; k < exceptions.length; k++) {
                sb.append((exceptions[k] instanceof Class) ?
                        ((Class) exceptions[k]).getName() :
                        exceptions[k].toString());
                if (k < (exceptions.length - 1))
                    sb.append(',');
            }
        }
        return sb.toString();
    }
}
