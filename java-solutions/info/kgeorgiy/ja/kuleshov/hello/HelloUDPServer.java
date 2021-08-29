package info.kgeorgiy.ja.kuleshov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private ExecutorService executorService;
    private DatagramSocket datagramSocket;

    @Override
    public void start(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        Runnable task = () -> {
            while (true) {
                DatagramPacket request;
                try {
                    request = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()],
                            datagramSocket.getReceiveBufferSize());
                    datagramSocket.receive(request);
                } catch (IOException e) {
                    break;
                }
                String str = HelloUtils.getResponse(new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8));
                byte[] body = str.getBytes();
                DatagramPacket response = new DatagramPacket(body, body.length, request.getSocketAddress());
                try {
                    datagramSocket.setSendBufferSize(body.length);
                    datagramSocket.send(response);
                } catch (IOException ignored) {
                }
            }
        };
        executorService = Executors.newFixedThreadPool(threads);
        for (int threadInd = 0; threadInd < threads; threadInd++) {
            executorService.submit(new Thread(task));
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        HelloUtils.mainServer(args, new HelloUDPServer());
    }
}
