package com.osuserverlist.crawler.servers.impl;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.shared.models.CrawlerType;
import com.osuserverlist.shared.models.EndpointType;

public class RippleApiV1 extends CrawlerAction {

    @Override
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) {
        ArrayList<CrawlerStatResponse> responses = new ArrayList<>();
        responses.add(new CrawlerStatResponse(CrawlerType.PLAYERCHECK, parseables[0].get("result").getAsLong()));  
        return responses;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.RIPPLEAPIV1;
    }
}
