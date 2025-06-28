package com.osuserverlist.crawler.servers.impl.collectors;

import java.util.List;

import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.shared.models.CrawlerType;

public class PeakNumberCollector implements INumberCollector{

    @Override
    public void collect(CrawlerStatResponse response) {
    
    }

    @Override
    public List<CrawlerType> getSupportedTypes() {
        return List.of(
            CrawlerType.PLAYERCHECK
        );
    }
    
}
