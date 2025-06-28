package com.osuserverlist.crawler;

import java.util.ArrayList;

import com.osuserverlist.shared.models.CrawlerType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrawlerStatResponse {
    private CrawlerType type;
    private long value;

    public static String fromArray(ArrayList<CrawlerStatResponse> stats) {
        if (stats == null || stats.isEmpty()) {
            return "No stats";
        }

        return stats.stream()
                .map(s -> s.getType().name() + ": " + s.getValue())
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
