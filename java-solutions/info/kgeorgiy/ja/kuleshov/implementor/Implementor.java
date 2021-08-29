package info.kgeorgiy.ja.kuleshov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * implementation JarImpler
 * @author Kuleshov Egor
 * */
public class Implementor implements JarImpler {

    private static String TABULATION_SYMBOL = "\t";
    private static final Map<Class<?>, String> PRIMITIVE_TYPE_TO_STRING_DEFAULT_VALUE = Map.of(
            byte.class, "0",
            short.class, "0",
            int.class, "0",
            long.class, "0L",
            float.class, "0.0f",
            double.class, "0.0d",
            char.class, "'\\u0000'",
            boolean.class, "false");

    /**
     * Gives a string representation of the parameters for the method
     *
     * @param parameters {@link Parameter[]} to be represented
     * @return a string with a representation of the parameters in the order passed to the function separated by commas
     */
    private static String getStringParameters(final Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(parameter -> Modifier.toString(parameter.getModifiers()) + " "
                        + parameter.getType().getCanonicalName() + " "
                        + parameter.getName())
                .collect(Collectors.joining(", "));
    }

    /**
     * Gives a string name of the exceptions
     *
     * @param exceptions {@link Exception} to be represented
     * @return a string name of the parameters in the order passed to the function separated by commas
     */
    private static String getStringThrows(final Class<?>[] exceptions) {
        return Arrays.stream(exceptions)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", "));
    }

    /**
     * gives a string of the default value of the function
     *
     * @param method {@link Method} to display the default value for
     * @return the default value string
     */
    private static String getStringDefaultValue(final Method method) {
        if (method.getReturnType() == Void.TYPE) {
            return "";
        }
        if (method.getDefaultValue() != null) {
            if (method.getDefaultValue().getClass().equals(String.class)) {
                return "\"" + method.getDefaultValue() + "\"";
            } else {
                return method.getDefaultValue().toString();
            }
        } else {
            if (method.getReturnType().isPrimitive()) {
                return PRIMITIVE_TYPE_TO_STRING_DEFAULT_VALUE.get(method.getReturnType());
            } else {
                return "null";
            }
        }
    }

    /**
     * gives a string representation of the implementation of a class method that ignores its arguments and returns a default value
     *
     * @param method {@link Method} to be implementation
     * @return string with method implementation
     */
    private static String getStringMethod(final Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(TABULATION_SYMBOL).append("@Override").append(System.lineSeparator());
        sb.append(TABULATION_SYMBOL).append(Modifier.toString(method.getModifiers()
                & (~(Modifier.ABSTRACT | Modifier.TRANSIENT)))).append(" ")
                .append(method.getReturnType().getCanonicalName()).append(" ")
                .append(method.getName());
        sb.append("(").append(getStringParameters(method.getParameters())).append(") ");
        if (method.getExceptionTypes().length > 0) {
            sb.append("throws ").append(getStringThrows(method.getExceptionTypes()));
        }
        sb.append("{").append(System.lineSeparator());
        sb.append(TABULATION_SYMBOL)
                .append(TABULATION_SYMBOL)
                .append("return ")
                .append(getStringDefaultValue(method))
                .append(";")
                .append(System.lineSeparator());
        sb.append("}");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * gives a string with the implementation of the class constructor, in which the corresponding constructor of the parent class is called
     *
     * @param token       type token type token to create implementation for.
     * @param constructor to create implementation for.
     * @return string with the implementation of the class constructor
     */
    private static String getStringConstructor(final Class<?> token, final Constructor<?> constructor) {
        StringBuilder sb = new StringBuilder();
        sb.append(TABULATION_SYMBOL).append(token.getSimpleName()).append("Impl").append(" (");
        sb.append(getStringParameters(constructor.getParameters()));
        sb.append(") ");
        if (constructor.getExceptionTypes().length > 0) {
            sb.append("throws ").append(getStringThrows(constructor.getExceptionTypes()));
        }
        sb.append("{").append(System.lineSeparator());
        sb.append(TABULATION_SYMBOL)
                .append(TABULATION_SYMBOL)
                .append("super(")
                .append(Arrays.stream(constructor.getParameters())
                        .map(Parameter::getName)
                        .collect(Collectors.joining(", ")));
        sb.append(");").append(System.lineSeparator());
        sb.append("}").append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * gives a string with the implementation of all previously unrealized methods.
     * For an abstract class, these are abstract methods; for an interface, they are not static methods.
     *
     * @param token type token type token to create implementation methods for.
     * @return a string with implemented methods {@link #getStringMethod(Method)} separated by an empty string.
     */
    private static String getStringMethods(Class<?> token) {
        StringBuilder sb = new StringBuilder();
        for (Method method : token.getMethods()) {
            if (token.isInterface() && !Modifier.isStatic(method.getModifiers()) ||
                    Modifier.isAbstract(token.getModifiers()) && Modifier.isAbstract(method.getModifiers())) {
                sb.append(getStringMethod(method))
                        .append(System.lineSeparator());
            }
        }
        for (Method method : token.getDeclaredMethods()) {
            if (Modifier.isAbstract(token.getModifiers())
                    && Modifier.isAbstract(method.getModifiers())
                    && Modifier.isProtected(method.getModifiers())) {
                sb.append(getStringMethod(method))
                        .append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * gives a string with implementations of all required constructors
     *
     * @param token type token type token to create implementation constructors for.
     * @return a string with implemented constructors {@link #getStringConstructor(Class, Constructor)}} separated by an empty string.
     * @throws ImplerException if the parent class does not contain any available constructors
     */
    private static String getStringConstructors(final Class<?> token) throws ImplerException {
        StringBuilder sb = new StringBuilder();
        if (token.isInterface()) {
            sb.append(TABULATION_SYMBOL).append(token.getSimpleName())
                    .append("Impl (){}").append(System.lineSeparator())
                    .append(System.lineSeparator());
        } else {
            int numConst = 0;
            for (Constructor<?> constructor : token.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    sb.append(getStringConstructor(token, constructor))
                            .append(System.lineSeparator());
                    numConst++;
                }
            }
            if (!token.isInterface() && numConst == 0) {
                throw new ImplerException(createErrorMessage(token, "no constructor available"));
            }
        }
        return sb.toString();
    }


    /**
     * gives a string with the implementation of the class or interface satisfying {@link info.kgeorgiy.java.advanced.implementor.Impler#implement(Class, Path)}
     * @param token type token type token to create implementation for.
     * @return a string with the implementation of a class or interface
     * @throws ImplerException if there is trouble with implementation
     */
    private static String writeClass(Class<?> token) throws ImplerException {
        if (token.getTypeParameters().length > 0) {
            throw new ImplerException(createErrorMessage(token, "it is generic class"));
        }
        if (Modifier.isFinal(token.getModifiers()) && !token.isInterface()) {
            throw new ImplerException(createErrorMessage(token, "it is final class"));
        }
        if (token.isPrimitive()) {
            throw new ImplerException(createErrorMessage(token, "it is primitive class"));
        }
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException(createErrorMessage(token, "it is private class"));
        }
        StringBuilder sb = new StringBuilder();
        if (token.getPackage() != null) {
            sb.append("package ").append(token.getPackageName()).append(";").append(System.lineSeparator());
        }
        sb.append("public class ").append(token.getSimpleName()).append("Impl ");
        if (token.isInterface()) {
            sb.append("implements ").append(token.getCanonicalName()).append(" ");
        } else {
            sb.append("extends ").append(token.getCanonicalName()).append(" ");
        }
        sb.append("{").append(System.lineSeparator());
        sb.append(getStringConstructors(token));
        sb.append(getStringMethods(token));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Path path = Paths.get(root.toString(), Paths.get(token.getPackageName()
                .replace('.', File.separatorChar))
                .toString(), token.getSimpleName() + "Impl.java");
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new ImplerException("can't create directories: " + e.getMessage());
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            StringBuilder result = new StringBuilder();
            String clazz = writeClass(token);
            for (int ind = 0; ind < clazz.length(); ++ind) {
                if (clazz.codePointAt(ind) >= 128) {
                    result.append("\\u").append(String.format("%04x", clazz.codePointAt(ind)));
                } else {
                    result.append(clazz.charAt(ind));
                }
            }
            bufferedWriter.write(result.toString());
        } catch (IOException e) {
            throw new ImplerException("write error: " + e.getMessage());
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tempDirPath = jarFile.getParent() == null ? Paths.get("implementorTemp") : jarFile.getParent().resolve("implementorTemp");
        Path tempClassPath = Paths.get(tempDirPath.toString(), Paths.get(token.getPackageName()
                .replace('.', File.separatorChar))
                .toString(), token.getSimpleName() + "Impl");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            implement(token, tempDirPath);
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
            final String classPath;
            try {
                classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            } catch (final URISyntaxException e) {
                throw new ImplerException("Unable to compile code: URL cannot be converted to URI.", e);
            }
            int compilationCode = javaCompiler.run(null, null, null, "-cp", classPath, tempClassPath.toString() + ".java");
            if (compilationCode != 0) {
                throw new ImplerException("can't compile class, code " + writeClass(token));
            }
            jarOutputStream.putNextEntry(new ZipEntry(token.getPackageName().replace('.', '/') + "/" + token.getSimpleName() + "Impl.class"));
            jarOutputStream.write(Files.readAllBytes(Paths.get(tempClassPath.toString() + ".class")));
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        } finally {
            if (Files.exists(tempDirPath)) {
                try {
                    Files.walkFileTree(tempDirPath,
                            new SimpleFileVisitor<>() {
                                @Override
                                public FileVisitResult postVisitDirectory(
                                        Path dir, IOException exc) throws IOException {
                                    Files.delete(dir);
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFile(
                                        Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    Files.delete(file);
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                } catch (IOException e) {
                    throw new ImplerException("can't delete temp: " + e.toString());
                }
            }
        }
    }

    /**
     * creates an error message when creating an implementation
     * @param token with an error occurred when creating the implementation
     * @param message about error
     * @return correct error message
     */
    private static String createErrorMessage(Class<?> token, String message) {
        return "can't creat implementation for class: " + token.getCanonicalName() + " " + message;
    }

    /**
     * print an error message to the error stream
     * @param message to print
     */
    private static void printError(String message) {
        System.err.println(message);
    }

    /**
     * main function of class. Create implementation for class.
     * @param args -jar classname file.jar
     *             or
     *             classname path
     */
    public static void main(String[] args) {
        if (args == null) {
            printError("expected 2/3 args, but arguments are null");
        } else if (args.length != 2 && args.length != 3) {
            printError("expected 2/3 args, but found: " + Arrays.toString(args));
        } else if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            printError("expected all arguments are not null");
        } else {
            try {
                Implementor implementor = new Implementor();
                if (args.length == 2) {
                    implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
                } else if (args[0].equals("-jar")) {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } else {
                    printError("wrong parameter " + args[0]);
                }
            } catch (ClassNotFoundException e) {
                printError("not found class: " + e.getMessage());
            } catch (ImplerException e) {
                printError(e.getMessage());
            } catch (InvalidPathException e) {
                printError("invalid path: " + e.getMessage());
            }
        }

    }
}
