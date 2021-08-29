package info.kgeorgiy.ja.kuleshov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HelloUDPClient implements HelloClient {
    private static int MAX_ATTEMPTS = 100;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        List<Thread> threadList = new ArrayList<>(threads);
        List<Queue<Runnable>> queuesTasksList = new ArrayList<>(threads);
        for (int threadInd = 0; threadInd < threads; threadInd++) {
            queuesTasksList.add(new ArrayDeque<>());
        }
        for (int threadInd = 0; threadInd < threads; threadInd++) {
            int finalThreadInd = threadInd;
            for (int requestInd = 0; requestInd < requests; requestInd++) {
                int finalRequestInd = requestInd;
                queuesTasksList.get(finalThreadInd).add(() -> {
                    final DatagramSocket datagramSocket;
                    try {
                        datagramSocket = new DatagramSocket();
                        datagramSocket.setSoTimeout(50);
                    } catch (SocketException e) {
                        System.err.println("bad connection: " + e);
                        return;
                    }
                    String strBody = HelloUtils.getRequest(prefix, finalThreadInd, finalRequestInd);
                    byte[] body = strBody.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(body, body.length, new InetSocketAddress(host, port));
                    DatagramPacket response = null;
                    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                        try {
                            datagramSocket.setSendBufferSize(body.length);
                            datagramSocket.send(datagramPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            response = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()],
                                    datagramSocket.getReceiveBufferSize());
                            datagramSocket.receive(response);
                        } catch (IOException ignored) {
                            continue;
                        }
                        if (new String(response.getData(),
                                response.getOffset(),
                                response.getLength(),
                                StandardCharsets.UTF_8).contains(strBody)) {
                            break;
                        }
                    }
                    if (response == null) {
                        return;
                    }

                    synchronized (System.out) {
                        System.out.println(strBody);
                        System.out.println(new String(response.getData(),
                                response.getOffset(),
                                response.getLength(),
                                StandardCharsets.UTF_8));
                    }
                    datagramSocket.disconnect();
                    datagramSocket.close();
                });
            }
        }
        for (int threadInd = 0; threadInd < threads; threadInd++) {
            int finalThreadInd = threadInd;
            threadList.add(new Thread(() -> {
                while (!queuesTasksList.get(finalThreadInd).isEmpty()) {
                    Objects.requireNonNull(queuesTasksList.get(finalThreadInd).poll()).run();
                }
            }));
            threadList.get(threadInd).start();
        }
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        HelloUtils.mainClient(args, new HelloUDPClient());
    }
}
