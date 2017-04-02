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
        public final ConcurrentLinkedQueue<Future<Document>> downloadTasks;
        public final ConcurrentLinkedQueue<Future<List<String>>> parseTasks;
        public final ConcurrentHashMap<String, Future<Document>> downloadCache;
        public final ConcurrentHashMap<Document, Future<List<String>>> parseCache;

        public CrawlerState() {
            downloadTasks = new ConcurrentLinkedQueue<>();
            parseTasks = new ConcurrentLinkedQueue<>();
            downloadCache = new ConcurrentHashMap<>();
            parseCache = new ConcurrentHashMap<>();
        }
    }

    private abstract class CrawlerTask<I, O> implements Callable<O> {

        protected final I data;
        protected final int depth;
        protected final CrawlerState state;

        CrawlerTask(I data, int depth, final CrawlerState state) {
            this.data = data;
            this.depth = depth;
            this.state = state;
        }

        protected abstract Future<O> getCachedValue(I data);

        protected abstract O getValue(I data) throws IOException;

        protected abstract void delegateTask(O result);

        @Override
        public O call() throws Exception {
            if (limitExceeded(data)) {
                return null;
            }
            Future<O> cached = getCachedValue(data);
            O result = cached != null && cached.isDone()
                    ? cached.get()
                    : getValue(data);
            if (depth > 1) {
                delegateTask(result);
            }
            return result;
        }

        protected abstract boolean limitExceeded(I data);
    }

    private class DownloadTask extends CrawlerTask<String, Document> {
        DownloadTask(String url, int depth, final CrawlerState state) {
            super(URLUtils.removeFragment(url), depth, state);
        }

        @Override
        protected Future<Document> getCachedValue(String data) {
            return state.downloadCache.get(data);
        }

        @Override
        protected Document getValue(String data) throws IOException {
            connections.put(data, connections.getOrDefault(data, 0) + 1);
            Document doc = downloader.download(data);
            connections.put(data, connections.getOrDefault(data, 0) - 1);
            return doc;
        }

        @Override
        protected void delegateTask(Document result) {
            Callable<List<String>> extractTask = new ExtractTask(result, depth, state);
            state.parseTasks.add(extractorsPool.submit(extractTask));
        }

        @Override
        protected boolean limitExceeded(String data) {
            if (connections.getOrDefault(data, 0) >= perHost) {
                Callable<Document> downloadTask = new DownloadTask(data, depth, state);
                state.downloadTasks.add(downloadersPool.submit(downloadTask));
                return true;
            }
            return false;
        }
    }

    private class ExtractTask extends CrawlerTask<Document, List<String>> {
        ExtractTask(Document document, int depth, final CrawlerState state) {
            super(document, depth, state);
        }

        @Override
        protected Future<List<String>> getCachedValue(Document data) {
            return state.parseCache.get(data);
        }

        @Override
        protected List<String> getValue(Document data) throws IOException {
            return data.extractLinks();
        }

        @Override
        protected void delegateTask(List<String> result) {
            for (String link : result) {
                Callable<Document> downloadTask = new DownloadTask(link, depth - 1, state);
                state.downloadTasks.add(downloadersPool.submit(downloadTask));
            }
        }

        @Override
        protected boolean limitExceeded(Document data) {
            return false;
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
        System.out.println("Downloading website " + url + " with depth = " + depth);

        CrawlerState state = new CrawlerState();

        Callable<Document> task = new DownloadTask(url, depth, state);
        state.downloadTasks.add(downloadersPool.submit(task));

        List<String> result = new ArrayList<>();
        Map<String, IOException> errors = new HashMap<>();

        while (!state.downloadTasks.isEmpty() || !state.parseTasks.isEmpty()) {
            while (!state.downloadTasks.isEmpty()) {
                Future<Document> document = state.downloadTasks.poll();
                while (document != null && !document.isDone() && !document.isCancelled()) {
                    try {
                        document.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            while (!state.parseTasks.isEmpty()) {
                Future<List<String>> list = state.parseTasks.poll();
                while (list != null && !list.isDone() && !list.isCancelled()) {
                    try {
                        result.addAll(list.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return new Result(result, errors);
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
        try {
            downloadersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            extractorsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
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
            System.out.println("WebCrawler initialized");
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
