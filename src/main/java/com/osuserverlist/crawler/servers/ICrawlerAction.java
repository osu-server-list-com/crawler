package com.osuserverlist.crawler.servers;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.shared.database.records.Endpoint;
import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.EndpointType;

public interface ICrawlerAction {
    public ArrayList<String> crawl(Server server, Endpoint endpoint) throws Exception;
    public ArrayList<CrawlerStatResponse> parse(JsonObject... parseables) throws JsonSyntaxException;
    public EndpointType getEndpointType();
}
