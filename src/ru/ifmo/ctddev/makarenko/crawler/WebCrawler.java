package ru.ifmo.ctddev.makarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final int perHost;
    private final ExecutorService downloadersPool, extractorsPool;
    private final Downloader downloader;
    private final ConcurrentHashMap<String, Integer> connections;

    private class CrawlerState {
        public final ConcurrentLinkedQueue<Future<Result>> tasks;
        public final ConcurrentHashMap<String, Document> downloadCache;
        public final ConcurrentHashMap<String, List<String>> parseCache;

        public CrawlerState() {
            tasks = new ConcurrentLinkedQueue<>();
            downloadCache = new ConcurrentHashMap<>();
            parseCache = new ConcurrentHashMap<>();
        }
    }

    private class DownloadTask implements Callable<Result> {

        protected final String url;
        protected final int depth;
        protected final CrawlerState state;

        DownloadTask(String url, int depth, final CrawlerState state) {
            this.url = url;
            this.depth = depth;
            this.state = state;
        }

        @Override
        public Result call() {
            final List<String> downloaded = new ArrayList<>();
            final Map<String, IOException> errors = new HashMap<>();

            Document cached = state.downloadCache.get(url);
            if (cached != null) {
                // Do nothing
            } else if (connections.getOrDefault(url, 0) >= perHost) {
                state.tasks.add(downloadersPool.submit(new DownloadTask(url, depth, state)));
            } else {
                try {
                    Document document = getValue(downloaded);
                    if (depth > 1) {
                        Callable<Result> extractTask = new ExtractTask(url, document, depth, state);
                        state.tasks.add(extractorsPool.submit(extractTask));
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                }
            }
            return new Result(downloaded, errors);
        }

        private Document getValue(final List<String> downloaded) throws IOException {
            connections.compute(url, (key, value) -> value == null ? 0 : value + 1);
            Document doc = downloader.download(url);
            connections.compute(url, (key, value) -> value == null || value == 0 ? 0 : value - 1);
            state.downloadCache.put(url, doc);
            // downloaded.add(url);
            return doc;
        }
    }

    private class ExtractTask implements Callable<Result> {

        protected final String url;
        protected final Document document;
        protected final int depth;
        protected final CrawlerState state;

        ExtractTask(String url, Document document, int depth, final CrawlerState state) {
            this.url = URLUtils.removeFragment(url);
            this.document = document;
            this.depth = depth;
            this.state = state;
        }

        @Override
        public Result call() throws Exception {
            final List<String> downloaded = new ArrayList<>();
            final Map<String, IOException> errors = new HashMap<>();

            try {
                List<String> result = getValue();
                if (depth > 1) {
                    for (String link : result) {
                        Callable<Result> downloadTask = new DownloadTask(link, depth - 1, state);
                        state.tasks.add(downloadersPool.submit(downloadTask));
                    }
                }
            } catch (IOException e) {
                errors.put(url, e);
            }
            return new Result(downloaded, errors);
        }

        private List<String> getValue() throws IOException {
            List<String> list = state.parseCache.get(url);
            if (list != null) {
                return list;
            }
            list = document.extractLinks();
            state.parseCache.put(url, list);
            return list;
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadersPool = Executors.newFixedThreadPool(downloaders);
        this.extractorsPool = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        this.connections = new ConcurrentHashMap<>();
    }

    /**
     * Метод download должен рекурсивно обходить страницы, начиная
     * с указанного URL на указанную глубину и возвращать список
     * загруженных страниц и файлов. Например, если глубина равна 1,
     * то должна быть загружена только указанная страница.
     * Если глубина равна 2, то указанная страница и те страницы и файлы,
     * на которые она ссылается и так далее. Загрузка и обработка страниц
     * (извлечение ссылок) должна выполняться максимально параллельно,
     * с учетом ограничений на число одновременно загружаемых страниц
     * (в том числе с одного хоста) и страниц, с которых загружаются ссылки.
     * Для распараллеливания разрешается создать до downloaders + extractors
     * вспомогательных потоков. Загружать и/или извлекать ссылки из одной
     * и той же страницы запрещается.
     */
    @Override
    public Result download(String url, int depth) {
        CrawlerState state = new CrawlerState();
        Future<Result> root = downloadersPool.submit(new DownloadTask(url, depth, state));
        state.tasks.add(root);

        List<String> downloaded = new ArrayList<>();
        Map<String, IOException> errors = new HashMap<>();

        while (!state.tasks.isEmpty()) {
            Future<Result> task = state.tasks.poll();
            do {
                try {
                    Result result = task.get();
                    downloaded.addAll(result.getDownloaded());
                    errors.putAll(result.getErrors());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } while (!task.isDone() && !task.isCancelled());
        }

        return new Result(downloaded, errors);
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
        try {
            if (!downloadersPool.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Downloader threads were not terminated properly");
            }
            if (!extractorsPool.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Link extractor threads were not terminated properly");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        int perHost = 100;

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
            System.out.println("WebCrawler initialized");
            Result result = crawler.download(url, 3);
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
