package com.osuserverlist.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.osuserverlist.shared.Globals;
import com.osuserverlist.shared.database.records.Server;

public class Crawler {

    public static final String SEPERATOR = "-------------------------------------------------------";

    private static final ExecutorService executor = Executors.newFixedThreadPool(Globals.serverConfig.crawler.workers);

    public static void start() {
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicLong totalCrawlDuration = new AtomicLong(0);
        AtomicInteger totalCrawlCount = new AtomicInteger(0);
        try {
            Globals.dsl.update(Globals.SERVER)
                    .set(Globals.SERVER.PING, (int) 0)
                    .set(Globals.SERVER.PLAYERS, (int) 0)
                    .set(Globals.SERVER.ONLINE, false)
                    .where(Globals.SERVER.LOCKED.eq(true))
                    .execute();
        } catch (Exception e) {
            App.logger.error("Error resetting locked servers: {}", e.getMessage());
        }

        List<Server> servers;
        try {
            servers = Globals.dsl
                    .select()
                    .from(Globals.SERVER)
                    .where(Globals.SERVER.LOCKED.eq(false))
                    .fetchInto(Server.class);
        } catch (Exception e) {
            App.logger.error("Error fetching servers from database: {}", e.getMessage());
            return;
        }

        App.logger.info(SEPERATOR);
        App.logger.info("Started crawling {} servers with {} threads", servers.size(),
                Globals.serverConfig.crawler.workers);
        App.logger.info(SEPERATOR);

        // Submit all tasks and collect futures to wait for completion
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Server server : servers) {
            try {
                CompletableFuture<CrawlerResponse> future = CompletableFuture.supplyAsync(new CrawlerTask(server),
                        executor);

                futures.add(future.thenAccept(response -> {
                    totalCount.incrementAndGet();
                    totalCrawlCount.incrementAndGet();
                    totalCrawlDuration.addAndGet(response.getTimeTaken());

                    if (response.getIncident() != null) {
                        IncidentHandler.addIncident(server, response.getIncident());
                    } else {
                        IncidentHandler.removeIncident(server);

                        App.logger.info("Crawled server {}: [{}], took {} ms", response.getServer().name(),
                                CrawlerStatResponse.fromArray(response.getStats()), response.getTimeTaken());
                    }

                    if (response.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                }));

            } catch (Exception e) {
                App.logger.error("Error submitting crawler task for server {}: {}", server.name(), e.getMessage());
            }
        }

        // Wait for all tasks to complete with a timeout
        try {
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allTasks.get(60, TimeUnit.SECONDS); // Wait max 60 seconds for all tasks

            App.logger.info(SEPERATOR);
            App.logger.info("All crawler tasks completed successfully.");
            App.logger.info("Total servers crawled: {} | success: {}, fail: {}", servers.size(), successCount.get(),
                    failureCount.get());
            App.logger.info("Total time taken: {} ms", System.currentTimeMillis() - startTime);
            App.logger.info("Average crawl duration: {} ms",
                    totalCrawlCount.get() > 0 ? totalCrawlDuration.get() / totalCrawlCount.get() : 0);
            App.logger.info(SEPERATOR);

        } catch (Exception e) {
            App.logger.warn("Some crawler tasks did not complete within timeout: {}", e.getMessage());
            // Cancel any remaining tasks
            for (CompletableFuture<Void> future : futures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }
    }

    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
