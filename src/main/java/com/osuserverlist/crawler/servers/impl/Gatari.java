package com.osuserverlist.crawler.servers.impl;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.shared.models.CrawlerType;
import com.osuserverlist.shared.models.EndpointType;

public class Gatari extends CrawlerAction {

    @Override
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) {
        ArrayList<CrawlerStatResponse> responses = new ArrayList<>();
        JsonObject resultElement = parseables[0].get("result").getAsJsonObject();
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYERCHECK, resultElement.get("online").getAsLong()));  
        responses.add(new CrawlerStatResponse(CrawlerType.REGISTERED_PLAYERS, resultElement.get("users").getAsLong()));  
        responses.add(new CrawlerStatResponse(CrawlerType.BANNED_PLAYERS, resultElement.get("banned").getAsLong()));  
        return responses;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.GATARIAPI;
    }
}
