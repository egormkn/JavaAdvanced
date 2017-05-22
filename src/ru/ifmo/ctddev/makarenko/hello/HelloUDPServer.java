package ru.ifmo.ctddev.makarenko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {

    private DatagramSocket datagramSocket;
    private ExecutorService executorService;

    /**
     * Run HelloUDPServer with specified port and number of threads
     *
     * @param args UDP Server arguments
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) {
                throw new IllegalArgumentException("HelloUDPServer needs 2 arguments");
            }
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            new HelloUDPServer().start(port, threads);
        } catch (IllegalArgumentException e) {
            System.err.println("Usage: HelloUDPServer <port> <threads>");
        }
    }

    private class ResponseTask implements Runnable {

        @Override
        public void run() {
            int bufferSize;
            try {
                bufferSize = datagramSocket.getReceiveBufferSize();
            } catch (SocketException e) {
                return;
            }
            byte[] buf = new byte[bufferSize];
            DatagramPacket request = new DatagramPacket(buf, buf.length);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    datagramSocket.receive(request);
                    String original = new String(request.getData(), 0, request.getLength(), Charset.forName("UTF-8"));
                    byte[] message = ("Hello, " + original).getBytes();
                    DatagramPacket response = new DatagramPacket(message, 0, message.length, request.getSocketAddress());
                    datagramSocket.send(response);
                } catch (IOException e) {
                    if (datagramSocket.isClosed()) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Start HelloUDPServer on specified port with at most <code>threads</code> threads
     *
     * @param port port to run the server
     * @param threads maximum number of threads
     */
    @Override
    public void start(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
            executorService = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; ++i) {
                executorService.submit(new ResponseTask());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop all threads and terminate the server
     * @apiNote https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
     */
    @Override
    public void close() {
        datagramSocket.close();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate in time");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        //executorService.shutdownNow();
    }
}
