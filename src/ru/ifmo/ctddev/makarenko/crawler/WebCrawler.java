package ru.ifmo.ctddev.makarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final int perHost;
    private final ExecutorService downloadersPool, extractorsPool;
    private final Downloader downloader;
    private final ConcurrentMap<String, Integer> connections;

    private class CrawlerState {
        final ConcurrentLinkedQueue<Future<Result>> tasks;
        final ConcurrentHashMap<String, Boolean> unique;

        CrawlerState() {
            tasks = new ConcurrentLinkedQueue<>();
            unique = new ConcurrentHashMap<>();
        }
    }

    private class DownloadTask implements Callable<Result> {

        private final String url, host;
        private final int depth;
        private final CrawlerState state;

        DownloadTask(String url, int depth, final CrawlerState state) {
            this.url = url;
            this.depth = depth;
            this.state = state;
            String host;
            try {
                host = URLUtils.getHost(url);
            } catch (MalformedURLException e) {
                host = url;
            }
            this.host = host;
        }

        @Override
        public Result call() {
            final List<String> downloaded = new ArrayList<>();
            final Map<String, IOException> errors = new HashMap<>();

            int threads = connections.compute(host, (key, value) -> {
                if (value == null) {
                    return 1;
                }
                int absValue = Math.abs(value);
                return absValue == perHost ? -absValue : absValue + 1;
            });

            if (threads > 0) {
                try {
                    Document document = downloader.download(url);
                    connections.compute(host, (key, value) -> Math.abs(value) - 1);
                    downloaded.add(url);
                    if (depth > 1) {
                        Callable<Result> extractTask = new ExtractTask(url, document, depth, state);
                        state.tasks.add(extractorsPool.submit(extractTask));
                    }
                } catch (IOException e) {
                    connections.compute(host, (key, value) -> Math.abs(value) - 1);
                    errors.put(url, e);
                }
            } else {
                state.tasks.add(downloadersPool.submit(new DownloadTask(url, depth, state)));
            }
            return new Result(downloaded, errors);
        }
    }

    private class ExtractTask implements Callable<Result> {

        final String url;
        final Document document;
        final int depth;
        final CrawlerState state;

        ExtractTask(String url, Document document, int depth, final CrawlerState state) {
            this.url = url;
            this.document = document;
            this.depth = depth;
            this.state = state;
        }

        @Override
        public Result call() {
            final List<String> downloaded = new ArrayList<>();
            final Map<String, IOException> errors = new HashMap<>();

            try {
                List<String> result = document.extractLinks();
                for (String link : result) {
                    link = URLUtils.removeFragment(link);
                    if (depth > 1 && state.unique.put(link, true) == null) {
                        Callable<Result> downloadTask = new DownloadTask(link, depth - 1, state);
                        state.tasks.add(downloadersPool.submit(downloadTask));
                    }
                }
            } catch (IOException e) {
                errors.put(url, e);
            }
            return new Result(downloaded, errors);
        }
    }

    @Override
    public Result download(String url, int depth) {
        url = URLUtils.removeFragment(url);

        CrawlerState state = new CrawlerState();
        state.unique.put(url, true);

        Callable<Result> downloadTask = new DownloadTask(url, depth, state);
        state.tasks.add(downloadersPool.submit(downloadTask));

        List<String> downloaded = new ArrayList<>();
        Map<String, IOException> errors = new HashMap<>();

        while (!state.tasks.isEmpty()) {
            Future<Result> task = state.tasks.poll();
            do {
                try {
                    Result result = task.get();
                    downloaded.addAll(result.getDownloaded());
                    errors.putAll(result.getErrors());
                } catch (InterruptedException | ExecutionException ignored) {
                }
            } while (!task.isDone() && !task.isCancelled());
        }

        return new Result(downloaded, errors);
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadersPool = Executors.newFixedThreadPool(Math.min(downloaders, 100));
        this.extractorsPool = Executors.newFixedThreadPool(Math.min(extractors, 100));
        System.out.println("D: " + downloaders + ", E: " + extractors);
        this.perHost = perHost;
        this.connections = new ConcurrentHashMap<>();
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
        shutdownAndAwaitTermination(downloadersPool);
        shutdownAndAwaitTermination(extractorsPool);
    }

    // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate in time");
                }
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            System.err.println("Usage: WebCrawler url [downloads [extractors [perHost]]]");
            return;
        }

        final int defaultPoolSize = Runtime.getRuntime().availableProcessors() * 2;

        final String url = args[0];
        int downloaders = defaultPoolSize;
        int extractors = defaultPoolSize;
        int perHost = 3;

        try {
            if (args.length > 3) {
                perHost = Integer.parseInt(args[3]);
            }
            if (args.length > 2) {
                extractors = Integer.parseInt(args[2]);
            }
            if (args.length > 1) {
                downloaders = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Wrong number format!");
            System.err.println("Usage: WebCrawler url [downloads [extractors [perHost]]]");
            return;
        }

        try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
            Result result = crawler.download(url, 2);
            for (String link : result.getDownloaded()) {
                System.out.println("OK: " + link);
            }
            for (Map.Entry<String, IOException> entry : result.getErrors().entrySet()) {
                System.out.println("FAIL: " + entry.getKey() + " - " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}