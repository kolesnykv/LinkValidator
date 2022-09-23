package com.knubisoft.utils;

import lombok.SneakyThrows;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class LinkManager {

    private final String link;
    private final String domain;
    private final ExecutorService executorService;

    public LinkManager(String link) {
        this.link = link;
        this.domain = getDomain(link);
        this.executorService = Executors.newCachedThreadPool();
    }

    @SneakyThrows
    public Map<String, Integer> collect() {
        ConcurrentMap<String, Integer> allLinks = new ConcurrentHashMap<>();
        try {
            collectFor(allLinks, Collections.singleton(link));
            return allLinks;
        } finally {
            shutdownAndAwaitTermination(executorService);
        }
    }

    private void collectFor(Map<String, Integer> allLinks, Set<String> links) {
        if (links.isEmpty()) {
            return;
        }

        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        for (String each : links) {
            if (each.contains(domain)) {
                futures.add(executorService.submit(new PageLinkCollector(each)));
            }
        }

        for (Future<Map<String, Integer>> each : futures) {
            try {
                Map<String, Integer> pageInfo = each.get();
                links = pageInfo.keySet();
                links.removeIf(allLinks::containsKey);
                allLinks.putAll(pageInfo);
                collectFor(allLinks, links);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @SneakyThrows
    private String getDomain(String link) {
        return new URI(link).getHost();
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
