package com.osuserverlist.crawler;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.shared.Globals;
import com.osuserverlist.shared.database.records.Endpoint;
import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.Incident;

public class CrawlerTask implements Supplier<CrawlerResponse> {


    private Server server;
    private long taskStartTime;

    public CrawlerTask(Server server) {
        this.server = server;
    }

    @Override
    public CrawlerResponse get() {
        taskStartTime = System.currentTimeMillis();
        try {
            long startTime = System.currentTimeMillis();
            String prefix = "[" + Thread.currentThread().getName() + "]";

            // Add a timeout check - if task runs longer than 30 seconds, abort
            long maxTaskDuration = 30000; // 30 seconds

            Endpoint endpoint = Globals.dsl.select()
                    .from(Globals.ENDPOINT)
                    .where(Globals.ENDPOINT.SRV_ID.eq(server.id()))
                    .and(Globals.ENDPOINT.TYPE.eq("PLAYERCHECK"))
                    .fetchOneInto(Endpoint.class);

            if (System.currentTimeMillis() - taskStartTime > maxTaskDuration) {
                return failCrawl("%s Task timeout for server %s before finding endpoint", 408, prefix, server.name());
            }

            if (endpoint == null) {
                return failCrawl("%s No endpoint found for server %s", 404, prefix, server.name());
            }

            CrawlerAction crawlerAction = App.supported.stream()
                    .filter(action -> action.getEndpointType() == endpoint.apitype())
                    .findFirst()
                    .orElse(null);

            if (crawlerAction == null) {
                return failCrawl("%s No crawler action found for endpoint type %s on server %s",
                        404,prefix, endpoint.apitype(), server.name());
            }

            long pingStartTime = System.currentTimeMillis();
            ArrayList<String> results = new ArrayList<>();
            try {
                if (System.currentTimeMillis() - taskStartTime > maxTaskDuration) {
                    return failCrawl("%s Task timeout for server %s before crawling", 408, prefix, server.name());
                }
                results = crawlerAction.crawl(server, endpoint);
            } catch (Exception e) {
                return failCrawl("%s Error crawling server %s: %s", 500, prefix, server.name(), e.getMessage());
            }

            if (System.currentTimeMillis() - taskStartTime > maxTaskDuration) {
                return failCrawl("%s Task timeout for server %s after crawling", 408, prefix, server.name());
            }

            if (results.isEmpty()) {
                return failCrawl("%s No results returned from crawler action for server %s", 404, prefix, server.name());
            }

            long pingEndTime = System.currentTimeMillis();
            ArrayList<JsonObject> jsonResults = new ArrayList<>();
            for (String result : results) {
                try {
                    JsonObject jsonObject = Globals.GSON.fromJson(result, JsonObject.class);
                    jsonResults.add(jsonObject);
                } catch (Exception e) {
                    // Silently skip invalid JSON results
                }
            }
            if (jsonResults.isEmpty()) {
                return failCrawl("%s No valid JSON objects parsed from results for server %s", 500, prefix, server.name());
            }

            ArrayList<CrawlerStatResponse> responses = new ArrayList<>();

            try {
                responses = crawlerAction.parse(jsonResults.toArray(new JsonObject[jsonResults.size()]));
            } catch (Exception e) {
                return failCrawl("%s Error parsing results for server %s: %s", 500, prefix, server.name(), e.getMessage());
            }

            if (responses.isEmpty()) {
                return failCrawl("%s No responses parsed from results for server %s", 404, prefix, server.name());
            }

            CrawlerResponse response = new CrawlerResponse(server, true, "Crawl successful", System.currentTimeMillis() - startTime);
            response.setStats(responses);
            if (server.players() == responses.get(0).getValue()) {
                return response;
            }

            try {
                Globals.dsl.update(Globals.SERVER)
                        .set(Globals.SERVER.PING, (int) (pingEndTime - pingStartTime))
                        .set(Globals.SERVER.PLAYERS, (int) responses.get(0).getValue())
                        .set(Globals.SERVER.ONLINE, true)
                        .where(Globals.SERVER.ID.eq(server.id()))
                        .execute();
            } catch (Exception e) {
                return failCrawl("%s Error updating database for server %s: %s", prefix, server.name(), e.getMessage());
            }

            return response;
        } catch (Exception e) {
            return failCrawl("Unexpected error in crawler task for server %s: %s",
                    server != null ? server.name() : "unknown", e.getMessage());
        }
    }

    private CrawlerResponse failCrawl(String message, Object... params) {
        String formatted = String.format(message, params);

        Globals.dsl.update(Globals.SERVER)
                .set(Globals.SERVER.PING, (int) 0)
                .set(Globals.SERVER.PLAYERS, (int) 0)
                .set(Globals.SERVER.ONLINE, false)
                .where(Globals.SERVER.ID.eq(server.id()))
                .execute();

        return new CrawlerResponse(server, false, formatted , System.currentTimeMillis() - taskStartTime, new Incident());
    }

    private CrawlerResponse failCrawl(String message, Integer responseCode, Object... params) {
        CrawlerResponse response = failCrawl(message, params);
        Incident indient = new Incident();
        indient.setTime(String.valueOf(System.currentTimeMillis()));
        indient.setMessage(String.format(message, params));
        indient.setUrl(server.url());
        indient.setResponseCode(responseCode);
        indient.setServer(server);
        response.setIncident(indient);
        return response;
    }

}
