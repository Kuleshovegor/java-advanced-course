package info.kgeorgiy.ja.kuleshov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaderExecutorService;
    private final ExecutorService parsingExecutorService;
    private final Map<String, Semaphore> hostToSemaphore;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaderExecutorService = Executors.newFixedThreadPool(downloaders);
        this.parsingExecutorService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        hostToSemaphore = new ConcurrentHashMap<>();
    }

    private Runnable getDownloadTask(String url,
                                     int depth,
                                     Set<String> visited,
                                     Queue<String> downloaded,
                                     Map<String, IOException> errors,
                                     Phaser phaser,
                                     String host) {
        return () -> {
            try {
                try {
                    hostToSemaphore.get(host).acquire();
                } catch (InterruptedException e) {
                    return;
                }
                final Document document = downloader.download(url);
                hostToSemaphore.get(host).release();
                downloaded.add(url);
                phaser.register();
                parsingExecutorService.submit(getParsingRecursiveTask(url, depth, visited, downloaded, errors, phaser, document));
            } catch (IOException e) {
                errors.put(url, e);
                hostToSemaphore.get(host).release();
            } finally {
                phaser.arrive();
            }
        };
    }

    private Runnable getParsingRecursiveTask(String url,
                                             int depth,
                                             Set<String> visited,
                                             Queue<String> downloaded,
                                             Map<String, IOException> errors,
                                             Phaser phaser,
                                             Document document) {
        return () -> {
            try {
                for (String link : document.extractLinks()) {
                    synchronized (visited) {
                        if (!visited.contains(link)) {
                            download(link, depth - 1, visited, downloaded, errors, phaser);
                        }
                    }
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        };
    }

    private void download(String url, int depth, Set<String> visited, Queue<String> downloaded, Map<String, IOException> errors, Phaser phaser) {
        if (depth == 0) {
            return;
        }
        visited.add(url);
        final String host;
        try {
            host = URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return;
        }
        hostToSemaphore.putIfAbsent(host, new Semaphore(perHost));
        phaser.register();
        downloaderExecutorService.submit(getDownloadTask(url, depth, visited, downloaded, errors, phaser, host));
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        final Queue<String> downloaded = new LinkedBlockingDeque<>();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        Phaser phaser = new Phaser(1);
        download(url, depth, visited, downloaded, errors, phaser);
        phaser.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        parsingExecutorService.shutdown();
        downloaderExecutorService.shutdown();
        try {
            if (!parsingExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                parsingExecutorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
        try {
            if (!downloaderExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                downloaderExecutorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private static int parseArgument(int index, String[] args) {
        if (index >= args.length) {
            return 1;
        }
        return Integer.parseInt(args[index]);
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("expected from 1 to 5 argument");
            return;
        }
        final int depth;
        final int downloads;
        final int extractors;
        final int perHost;
        try {
            depth = parseArgument(1, args);
            downloads = parseArgument(2, args);
            extractors = parseArgument(3, args);
            perHost = parseArgument(4, args);
        } catch (NumberFormatException e) {
            System.err.println("bad argument: " + e.getMessage());
            return;
        }
        try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost)) {
            final Result result = webCrawler.download(args[0], depth);
            System.out.println("Downloaded OK:");
            System.out.println(result.getDownloaded()
                    .stream()
                    .collect(Collectors.joining(System.lineSeparator())));
            System.out.println("Errors:");
            System.out.println(result.getErrors().entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            System.err.println("download error: " + e.getMessage());
        }
    }
}
