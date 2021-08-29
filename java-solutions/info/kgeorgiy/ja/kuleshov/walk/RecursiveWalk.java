package info.kgeorgiy.ja.kuleshov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Expected: \"RecursiveWalk <inputFile> <outputFile>\"");
            return;
        }

        final Path inputFile;
        try {
            inputFile = Paths.get(args[0]);
        } catch (InvalidPathException exc) {
            System.err.println("Invalid input file " + args[0] + " " + exc.getMessage());
            return;
        }
        final Path outputFile;
        try {
            outputFile = Paths.get(args[1]);
        } catch (InvalidPathException exc) {
            System.err.println("Invalid output file " + args[1] + " " + exc.getMessage());
            return;
        }

        final Path outputFileDir = outputFile.getParent();
        if (outputFileDir != null && !outputFileDir.toFile().exists()) {
            try {
                Files.createDirectories(outputFileDir);
            } catch (IOException exc) {
                System.err.println("Can't creat directories " + outputFileDir + " " + exc.getMessage());
                return;
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final Path file;
                    try {
                        file = Paths.get(line);
                    } catch (InvalidPathException exc) {
                        System.err.println("Invalid file " + line + " " + exc.getMessage());
                        writer.write(String.format("%016x", 0) + " " + line + System.lineSeparator());
                        continue;
                    }
                    Files.walkFileTree(file, new SimpleFileVisitor<>() {

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            System.err.println("File view error. File: " + file + " error: " + exc.getMessage());
                            writer.write(String.format("%016x", 0) + " " + file + System.lineSeparator());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            writer.write(String.format("%016x", hashPJW(file)) + " " + file + System.lineSeparator());
                            return FileVisitResult.CONTINUE;
                        }

                        private long hashPJW(final Path file) throws IOException {
                            long hash = 0;
                            final int BITS = 64;
                            final int bufferSize = 1024;
                            byte[] bytes = new byte[bufferSize];
                            int len;
                            try (InputStream inputStream = Files.newInputStream(file)) {
                                while ((len = inputStream.read(bytes, 0, bufferSize)) > 0) {
                                    for (int i = 0; i < len; ++i) {
                                        hash = (hash << (BITS / 8)) + (bytes[i] & 0xff);
                                        final long high = hash & 0xff00_0000_0000_0000L;
                                        if (high != 0) {
                                            hash ^= (high >> (BITS * 3 / 4));
                                            hash &= (~high);
                                        }
                                    }
                                }
                            }
                            return hash;
                        }
                    });
                }
            } catch (IOException writeExc) {
                System.err.println("Write error. File: " + outputFile + " Error:" + writeExc.getMessage());
            }
        } catch (IOException readExc) {
            System.err.println("Read error. File: " + inputFile + " Error:" + readExc.getMessage());
        }
    }
}