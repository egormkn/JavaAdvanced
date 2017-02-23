package ru.ifmo.ctddev.makarenko.implementor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class ClassPrinter {

    private final Class token;
    private final Appendable output;
    private final Set<Package> imports;

    public ClassPrinter(Class<?> token, Appendable output) {
        this.token = token;
        this.output = output;
        imports = new HashSet<>();
    }

    public void print() throws IOException {
        printPackage(token.getPackage());
        printImports();
        printClass();
    }

    private void printPackage(Package pack) throws IOException {
        if (pack != null) {
            output
                    .append("package ")
                    .append(pack.getName())
                    .append(";")
                    .append("\n\n");
        }
    }

    private void printImports() throws IOException {
        for (Method method : token.getDeclaredMethods()) {
            Package pack = method.getReturnType().getPackage();
            if (pack != null && !imports.contains(pack)) {
                imports.add(pack);
            }
        }
        for (Package pack : imports) {
            if (pack.equals(token.getPackage())) {
                continue;
            }
            output
                    .append("import ")
                    .append(pack.getName())
                    .append(".*;")
                    .append('\n');
        }
    }

    private void printClass() throws IOException {
        output
                .append(toGenericString(token))
                .append(" {")
                .append("\n\n");

        for (Method method : token.getDeclaredMethods()) {
            output.append("\t").append(toGenericString(method)).append(" {\n");

            output.append("\t\n\t}\n\n");
        }

        output.append("}\n");
    }

    private String toGenericString(Class<?> c) {
        if (c.isPrimitive()) {
            return c.toString();
        } else {
            StringBuilder sb = new StringBuilder();

            // Class modifiers are a superset of interface modifiers
            int modifiers = c.getModifiers() & Modifier.classModifiers();
            if (modifiers != 0) {
                sb.append(Modifier.toString(modifiers));
                sb.append(' ');
            }

            if (c.isAnnotation()) {
                sb.append('@');
            }
            if (c.isInterface()) { // Note: all annotation types are interfaces
                sb.append("interface");
            } else {
                if (c.isEnum()) {
                    sb.append("enum");
                } else {
                    sb.append("class");
                }
            }
            sb.append(' ');
            sb.append(c.getSimpleName());
            sb.append(Implementor.IMPL_SUFFIX);

            TypeVariable<?>[] typeparms = c.getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append(typeparm.getTypeName());
                    first = false;
                }
                sb.append('>');
            }

            return sb.toString();
        }
    }

    private String toGenericString(Method method) {
        int mask = Modifier.methodModifiers();
        boolean isDefault = method.isDefault();

        try {
            StringBuilder sb = new StringBuilder();

            int mod = method.getModifiers() & mask;
            int ACCESS_MODIFIERS =
                    Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;


            if (mod != 0 && !isDefault) {
                sb.append(Modifier.toString(mod)).append(' ');
            } else {
                int access_mod = mod & ACCESS_MODIFIERS;
                if (access_mod != 0)
                    sb.append(Modifier.toString(access_mod)).append(' ');
                if (isDefault)
                    sb.append("default ");
                mod = (mod & ~ACCESS_MODIFIERS);
                if (mod != 0)
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
            sb.append(method.getDeclaringClass().getTypeName()).append('.');
            sb.append(method.getName());

            sb.append('(');
            Type[] params = method.getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = params[j].getTypeName();
                if (method.isVarArgs() && (j == params.length - 1)) // replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(',');
            }
            sb.append(')');
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
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }
}
