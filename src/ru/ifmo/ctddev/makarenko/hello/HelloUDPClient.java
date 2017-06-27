package ru.ifmo.ctddev.makarenko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class HelloUDPClient implements HelloClient {

    /**
     * List of possible answers (chosen by random)
     */
    private static final List<String> ANSWER = Arrays.asList("%s Hello", "%s ආයුබෝවන්", "Բարեւ, %s", "مرحبا %s", "Салом %s", "Здраво %s", "Здравейте %s", "Прывітанне %s", "Привіт %s", "Привет, %s", "Поздрав %s", "سلام به %s", "שלום %s", "Γεια σας %s", "העלא %s", "ہیل%s٪ ے", "Bonjou %s", "Bonjour %s", "Bună ziua %s", "Ciao %s", "Dia duit %s", "Dobrý deň %s", "Dobrý den, %s", "Habari %s", "Halló %s", "Hallo %s", "Halo %s", "Hei %s", "Hej %s", "Hello  %s", "Hello %s", "Hello %s", "Helo %s", "Hola %s", "Kaixo %s", "Kamusta %s", "Merhaba %s", "Olá %s", "Ola %s", "Përshëndetje %s", "Pozdrav %s", "Pozdravljeni %s", "Salom %s", "Sawubona %s", "Sveiki %s", "Tere %s", "Witaj %s", "Xin chào %s", "ສະບາຍດີ %s", "สวัสดี %s", "ഹലോ %s", "ಹಲೋ %s", "హలో %s", "हॅलो %s", "नमस्कार%sको", "হ্যালো %s", "ਹੈਲੋ %s", "હેલો %s", "வணக்கம் %s", "ကို %s မင်္ဂလာပါ", "გამარჯობა %s", "ជំរាបសួរ %s បាន", "こんにちは%s", "你好%s", "안녕하세요  %s");

    /**
     * Run HelloUDPClient with specified host, port, request prefix,
     * number of requests and number of threads
     *
     * @param args UDP Client arguments
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 5 || args[0] == null || args[2] == null) {
                throw new IllegalArgumentException("Need 5 arguments");
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int requests = Integer.parseInt(args[3]);
            int threads = Integer.parseInt(args[4]);
            new HelloUDPClient().run(host, port, prefix, requests, threads);
        } catch (IllegalArgumentException e) {
            System.err.println("Usage: HelloUDPClient <host> <port> <prefix> <requests> <threads>");
        }
    }

    private class RequestTask implements Callable<Object> {

        final InetAddress address;
        final int port;
        final String prefix;
        final int requests;
        final int threadID;

        public RequestTask(InetAddress address, int port, String prefix, int requests, int threadID) {
            this.address = address;
            this.port = port;
            this.prefix = prefix;
            this.requests = requests;
            this.threadID = threadID;
        }

        @Override
        public Object call() throws Exception {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {
                datagramSocket.setSoTimeout(500);
                byte[] buf = new byte[datagramSocket.getReceiveBufferSize()];
                DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
                for (int i = 0; i < requests && !Thread.currentThread().isInterrupted(); ++i) {
                    String messageString = prefix + Integer.toString(threadID) + "_" + Integer.toString(i);
                    byte[] messageBytes = messageString.getBytes();
                    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
                    datagramSocket.send(packet);

                    boolean ready = false;
                    while (!ready && !Thread.currentThread().isInterrupted()) {
                        try {
                            datagramSocket.receive(inPacket);
                            String receivedMessage = new String(inPacket.getData(), 0, inPacket.getLength(), Charset.forName("UTF-8"));
                            if (ANSWER.stream().anyMatch(s -> String.format(s, messageString).equals(receivedMessage))) {
                                ready = true;
                            }
                        } catch (SocketTimeoutException | NumberFormatException e) {
                            datagramSocket.send(packet);
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * Start HelloUDPClient with specified parameters
     *
     * @param host     host to which requests will be sent
     * @param port     port to which requests will be sent
     * @param prefix   request prefix
     * @param requests number of requests
     * @param threads  maximum number of threads
     */
    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        Queue<Future<Object>> tasks = new LinkedList<>();

        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            for (int i = 0; i < threads; ++i) {
                tasks.add(executorService.submit(new RequestTask(inetAddress, port, prefix, requests, i)));
            }
            while (!tasks.isEmpty()) {
                tasks.poll().get();
            }
            executorService.shutdownNow();
        } catch (UnknownHostException | InterruptedException | ExecutionException | SecurityException ignored) {
        }
    }
}
