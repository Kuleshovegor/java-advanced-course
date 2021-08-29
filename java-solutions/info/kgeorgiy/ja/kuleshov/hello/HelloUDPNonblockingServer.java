package info.kgeorgiy.ja.kuleshov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class HelloUDPNonblockingServer implements HelloServer {
    private static final int BUFFER_SIZE = 1024;
    private ExecutorService workers;
    private ExecutorService distributor;
    private Selector selector;
    private DatagramChannel datagramChannel;
    private BlockingQueue<Response> responseQueue;

    @Override
    public void start(int port, int threads) {
        responseQueue = new LinkedBlockingQueue<>();
        workers = Executors.newFixedThreadPool(threads);
        distributor = Executors.newSingleThreadExecutor();
        try {
            selector = Selector.open();
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
            datagramChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new HelloUDPServerException("can't create channel: " + e.getMessage());
        }
        distributor.execute(distributionTask);
    }

    private static class Response {
        private final ByteBuffer buffer;
        private final SocketAddress address;

        public Response(ByteBuffer buffer, SocketAddress address) {
            this.buffer = buffer;
            this.address = address;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public SocketAddress getAddress() {
            return address;
        }
    }

    private static Response getResponse(final ByteBuffer buffer, final SocketAddress address) {
        String response = HelloUtils.getResponse(HelloUtils.bufferToString(buffer));
        ByteBuffer responseByteBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        return new Response(responseByteBuffer, address);
    }

    @Override
    public void close() {
        try {
            datagramChannel.close();
            selector.close();
        } catch (IOException e) {
            throw new HelloUDPServerException("can't close channel: " + e.getMessage());
        }
        workers.shutdown();
        distributor.shutdown();
        try {
            if (!workers.awaitTermination(10, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
        try {
            if (!distributor.awaitTermination(10, TimeUnit.SECONDS)) {
                distributor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private Runnable distributionTask = () -> {
        while (!Thread.interrupted() && datagramChannel.isOpen() && selector.isOpen()) {
            final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            try {
                do {
                    selector.select();
                    // :NOTE: selectedKeys().isEmpty
                } while (selector.isOpen() && selector.selectedKeys().size() == 0);
            } catch (IOException e) {
                throw new HelloUDPServerException("selector select error: " + e.getMessage());
            }
            if (!selector.isOpen()) {
                break;
            }
            final SelectionKey key = selector.selectedKeys().iterator().next();
            selector.selectedKeys().remove(key);
            if (key.isReadable()) {
                final SocketAddress socketAddress;
                try {
                    socketAddress = ((DatagramChannel) key.channel()).receive(buffer);
                } catch (IOException ignored) {
                    continue;
                }
                buffer.flip();
                workers.submit(() -> {
                    try {
                        responseQueue.put(getResponse(buffer, socketAddress));
                    } catch (InterruptedException ignored) {
                    }
                    key.interestOpsOr(SelectionKey.OP_WRITE);
                    selector.wakeup();
                });
            }
            if (key.isValid() && key.isWritable()) {
                if (!responseQueue.isEmpty()) {
                    final Response response = responseQueue.poll();
                    try {
                        ((DatagramChannel) key.channel()).send(response.getBuffer(), response.getAddress());
                    } catch (IOException ignored) {
                    }
                } else {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    };

    public static void main(String[] args) {
        HelloUtils.mainServer(args, new HelloUDPServer());
    }
}
