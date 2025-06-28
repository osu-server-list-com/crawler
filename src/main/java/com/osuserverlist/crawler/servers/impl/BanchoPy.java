package com.osuserverlist.crawler.servers.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.shared.Globals;
import com.osuserverlist.shared.database.records.Endpoint;
import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.CrawlerType;
import com.osuserverlist.shared.models.EndpointType;

import okhttp3.Request;
import okhttp3.Response;

public class BanchoPy extends CrawlerAction {

    @Override
    public ArrayList<String> crawl(Server server, Endpoint endpoint) throws Exception {
        ArrayList<String> results = super.crawl(server, endpoint);

        Endpoint custom = Globals.dsl.select()
                .from(Globals.ENDPOINT)
                .where(Globals.ENDPOINT.SRV_ID.eq(server.id()))
                .and(Globals.ENDPOINT.TYPE.eq("CUSTOM"))
                .and(Globals.ENDPOINT.APITYPE.eq(EndpointType.BANCHOPY.toString()))
                .fetchOneInto(Endpoint.class);

        if (custom == null) {
            return results;
        }

        List<String> toCrawl = List.of("/v2/maps", "/v2/clans", "/v2/scores");

        for (String crawlable : toCrawl) {
            Request request = new Request.Builder().url(custom.endpoint() + crawlable).build();

            Response response = Globals.HTTP_CLIENT.newCall(request).execute();
            results.add(response.body().string());
        }

        return results;
    }

    @Override
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) {
        ArrayList<CrawlerStatResponse> responses = new ArrayList<>();
        JsonObject resultElement = parseables[0].get("counts").getAsJsonObject();
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYERCHECK, resultElement.get("online").getAsLong()));
        responses.add(new CrawlerStatResponse(CrawlerType.REGISTERED_PLAYERS, resultElement.get("total").getAsLong()));

        if (parseables.length < 4)
            return responses;

        JsonObject customMapsElement = parseables[1].get("meta").getAsJsonObject();
        responses.add(new CrawlerStatResponse(CrawlerType.MAPS, customMapsElement.get("total").getAsLong()));
        JsonObject customClansElement = parseables[2].get("meta").getAsJsonObject();
        responses.add(new CrawlerStatResponse(CrawlerType.CLANS, customClansElement.get("total").getAsLong()));
        JsonObject customScoresElement = parseables[3].get("meta").getAsJsonObject();
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYS, customScoresElement.get("total").getAsLong()));
        return responses;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.BANCHOPY;
    }
}
