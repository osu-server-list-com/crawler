package com.osuserverlist.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.osuserverlist.shared.database.records.Server;
import com.osuserverlist.shared.models.Incident;

public class IncidentHandler {
    private static final List<Server> incidents = Collections.synchronizedList(new ArrayList<>());

    public static void addIncident(Server server, Incident incident) {
        if (server != null && !incidents.contains(server)) {
            incidents.add(server);
        }

        App.logger.warn("Incident detected for server {}: {} ({}): {}",
                                server.name(), incident.getTime(),
                                incident.getResponseCode(), incident.getMessage());
    }

    public static void removeIncident(Server server) {
        if (server != null && incidents.contains(server)) {
            App.logger.warn("Incident resolved for server {}", server.name());
            incidents.remove(server);
        }
      
    }
}
