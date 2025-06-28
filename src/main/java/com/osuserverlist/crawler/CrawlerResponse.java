package com.osuserverlist.crawler;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.Incident;

import lombok.Data;

@Data
public class CrawlerResponse {
    private Server server;
    private boolean success;
    private String message;
    private long timeTaken;

    @Nullable
    private Incident incident;

    private ArrayList<CrawlerStatResponse> stats = new ArrayList<>();

    public CrawlerResponse(Server server, boolean success, String message, long timeTaken) {
        this.server = server;
        this.success = success;
        this.message = message;
        this.timeTaken = timeTaken;
    }

    public CrawlerResponse(Server server, boolean success, String message, long timeTaken, Incident incident) {
        this.server = server;
        this.success = success;
        this.message = message;
        this.timeTaken = timeTaken;
        this.incident = incident;
    }
}
