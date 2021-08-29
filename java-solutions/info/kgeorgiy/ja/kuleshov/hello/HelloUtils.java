package info.kgeorgiy.ja.kuleshov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HelloUtils {
    public static String getRequest(String prefix, int threadIndex, int requestIndex) {
        return prefix + threadIndex + "_" + requestIndex;
    }

    public static String getResponse(String request) {
        return "Hello, " + request;
    }

    public static String bufferToString(ByteBuffer buffer) {
        return new String(buffer.array(), buffer.position(), buffer.limit(), StandardCharsets.UTF_8);
    }

    public static void mainClient(String[] args, HelloClient client) {
        // :NOTE: args != null || args.none { it == null }
        if (args.length != 5) {
            System.err.println("Expected 5 arguments, but found " + args.length);
            return;
        }
        String address;
        int port;
        String prefix;
        int threads;
        int requests;
        try {
            address = args[0];
            port = Integer.parseInt(args[1]);
            prefix = args[2];
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("bad argument: " + e.getMessage());
            return;
        }
        client.run(address, port, prefix, threads, requests);
    }

    public static void mainServer(String[] args, HelloServer server) {
        if (args.length != 2) {
            System.err.println("Expected 2 arguments, but found " + args.length);
            return;
        }
        int port;
        int threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Bad argument: " + e.getMessage());
            return;
        }
        server.start(port, threads);
    }
}
