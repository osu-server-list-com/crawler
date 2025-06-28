package com.osuserverlist.crawler.servers.impl;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.shared.models.CrawlerType;
import com.osuserverlist.shared.models.EndpointType;

public class Iceberg extends CrawlerAction {

    @Override
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) {
        ArrayList<CrawlerStatResponse> responses = new ArrayList<>();
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYERCHECK, parseables[0].get("online_users").getAsLong()));  
        responses.add(new CrawlerStatResponse(CrawlerType.REGISTERED_PLAYERS, parseables[0].get("total_users").getAsLong()));  
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYS, parseables[0].get("total_scores").getAsLong()));  
        return responses;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.ICEBERG;
    }
}
