package com.osuserverlist.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.crawler.servers.CrawlerAction;
import com.osuserverlist.crawler.servers.impl.BanchoPy;
import com.osuserverlist.crawler.servers.impl.Gatari;
import com.osuserverlist.crawler.servers.impl.Iceberg;
import com.osuserverlist.crawler.servers.impl.Ragnarok;
import com.osuserverlist.crawler.servers.impl.RippleApiV1;
import com.osuserverlist.crawler.servers.impl.RippleApiV2;
import com.osuserverlist.crawler.servers.impl.Sunrise;
import com.osuserverlist.shared.Globals;
import com.osuserverlist.shared.database.Database;
import com.osuserverlist.shared.utils.DiscordWebhook;

/**
 * OSL Crawler v3 i guess!
 *
 */
public class App {
    public static Logger logger = LoggerFactory.getLogger("OSL-Crawler");
    static long startTime = System.currentTimeMillis();

    static final List<CrawlerAction> supported = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Osu Server List - Crawler - Developement Build!");  
     
        Database.connect(Globals.serverConfig);

        DiscordWebhook webhookBuilder = new DiscordWebhook.Builder(Globals.serverConfig.crawler.webhook)
            .setContent("Osu Server List Crawler started!")
            .build();

        webhookBuilder.send();        

        supported.add(new RippleApiV1());
        supported.add(new BanchoPy());
        supported.add(new Sunrise());
        supported.add(new Iceberg());
        supported.add(new Gatari());
        supported.add(new Ragnarok());
        supported.add(new RippleApiV2());

        

        logger.info("-> Ignited in " + (System.currentTimeMillis() - startTime) + "ms");

        // Add shutdown hook to properly close the executor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down crawler...");
            Crawler.shutdown();
        }));

        while(true) {

            Crawler.start();
            Thread.sleep(Globals.serverConfig.crawler.timeout * 1000);
        }
    }
}
