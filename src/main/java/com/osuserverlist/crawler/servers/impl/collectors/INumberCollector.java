package com.osuserverlist.crawler.servers.impl.collectors;

import java.util.List;

import com.osuserverlist.crawler.CrawlerStatResponse;
import com.osuserverlist.shared.models.CrawlerType;

public interface INumberCollector {
    public void collect(CrawlerStatResponse response);

    public List<CrawlerType> getSupportedTypes();
}
