package com.osuserverlist.crawler.servers;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.shared.Globals;
import com.osuserverlist.shared.database.records.Endpoint;
import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.EndpointType;

import okhttp3.Request;
import okhttp3.Response;

public class CrawlerAction implements ICrawlerAction {

    @Override
    public ArrayList<String> crawl(Server server, Endpoint endpoint) throws Exception {
        ArrayList<String> results = new ArrayList<>();

        Request request = new Request.Builder().url(endpoint.endpoint()).build();

        Response response = Globals.HTTP_CLIENT.newCall(request).execute();
        results.add(response.body().string());

        return results;
    }

    @Override
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) throws JsonSyntaxException {
        ArrayList<CrawlerStatResponse> responses = new ArrayList<>();
        return responses;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.NONE;
    }

}
