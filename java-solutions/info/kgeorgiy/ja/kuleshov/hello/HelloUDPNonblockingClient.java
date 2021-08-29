package info.kgeorgiy.ja.kuleshov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class HelloUDPNonblockingClient implements HelloClient {
    private static final int MAX_ATTEMPTS = 1000;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        final Selector selector;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new HelloUDPClientException("selector creat error: " + e.getMessage());
        }
        final SocketAddress inetSocketAddress = new InetSocketAddress(0);
        final SocketAddress serverAddress = new InetSocketAddress(host, port);
        for (int i = 0; i < threads; i++) {
            try {
                final DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                datagramChannel.bind(inetSocketAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE, new ThreadInformation(i));
            } catch (IOException e) {
                // :NOTE: exception
                throw new RuntimeException(e);
            }
        }
        int responseCount = 0;
        while (responseCount < requests * threads) {
            try {
                // :NOTE: move to a const value SOCKET_TIMEOUT_IN_MILLIS
                selector.select(50);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            if (selector.selectedKeys().isEmpty()) {
                for (SelectionKey key : selector.keys()) {
                    ThreadInformation threadInformation = (ThreadInformation) key.attachment();
                    threadInformation.incrementAttempts();
                    if (threadInformation.getAttempts() >= MAX_ATTEMPTS) {
                        throw new HelloUDPClientException("server is not available");
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                }
                continue;
            }
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                final SelectionKey key = iterator.next();
                iterator.remove();
                final ThreadInformation threadInformation = (ThreadInformation) key.attachment();
                final String request = prefix + threadInformation.getTheadIndex() + "_" + threadInformation.getResponsed();
                if (key.isWritable() && key.isValid()) {
                    final ByteBuffer buffer = ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8));
                    try {
                        ((DatagramChannel) key.channel()).send(buffer, serverAddress);
                    } catch (IOException ignored) {
                    }
                    key.interestOpsAnd(~SelectionKey.OP_WRITE);
                    key.interestOpsOr(SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    // :NOTE: const bufferSize, magic numbers
                    final int bufferSize = prefix.length() * 2 + 128;
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
                    try {
                        ((DatagramChannel) key.channel()).receive(byteBuffer);
                    } catch (IOException ignored) {
                        continue;
                    }
                    byteBuffer.flip();
                    final String response = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit(), StandardCharsets.UTF_8);
                    if (response.contains(request)) {
                        threadInformation.incrementResponsed();
                        responseCount++;
                        threadInformation.resetAttempts();
                        synchronized (System.out) {
                            System.out.println(request);
                            System.out.println(response);
                        }
                        if (threadInformation.getResponsed() == requests) {
                            try {
                                ((DatagramChannel) key.channel()).disconnect();
                                key.channel().close();
                            } catch (IOException e) {
                                throw new HelloUDPClientException("can't close channel: " + e.getMessage());
                            }
                            continue;
                        }
                    }
                    key.interestOpsOr(SelectionKey.OP_WRITE);
                    key.interestOpsAnd(~SelectionKey.OP_READ);
                }
            }
        }
    }

    private static class ThreadInformation {
        private int theadIndex;
        private int responsed;
        private int attempts;

        public ThreadInformation(int theadIndex) {
            this.theadIndex = theadIndex;
            this.responsed = 0;
            this.attempts = 0;
        }

        public int getTheadIndex() {
            return theadIndex;
        }

        public int getResponsed() {
            return responsed;
        }

        public int getAttempts() {
            return attempts;
        }

        public void incrementResponsed() {
            responsed++;
        }

        public void incrementAttempts() {
            attempts++;
        }

        public void resetAttempts() {
            attempts = 0;
        }

        @Override
        public String toString() {
            return "ThreadInformation{" +
                    "theadIndex=" + theadIndex +
                    ", responsed=" + responsed +
                    '}';
        }
    }

    public static void main(String[] args) {
        HelloUtils.mainClient(args, new HelloUDPNonblockingClient());
    }
}
