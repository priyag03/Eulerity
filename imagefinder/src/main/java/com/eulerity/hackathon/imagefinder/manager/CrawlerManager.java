package com.eulerity.hackathon.imagefinder.manager;

import com.eulerity.hackathon.imagefinder.crawler.WebCrawler;
import com.eulerity.hackathon.imagefinder.crawler.PlaywrightCrawler;

import java.util.HashSet;
import java.util.Set;

public class CrawlerManager {
    
    private WebCrawler crawler;

    public CrawlerManager(WebCrawler crawler) {
        this.crawler = crawler;
    }

    public void startCrawling(String startUrl, int maxSubpages) {
        System.out.println("Started Executing CrawlerManager");
        Set<String> images = new HashSet<String>();
        crawler.crawl(startUrl, maxSubpages, images);

        System.out.println("\n===================================");
        System.out.println("✅ TOTAL UNIQUE IMAGES FOUND: " + images.size());
        System.out.println("===================================");
    }
    public Set<String> startCrawlingAndReturnImages(String startUrl, int maxSubpages) {
    Set<String> images = new HashSet<String>();
    crawler.crawl(startUrl, maxSubpages, images);
    return images;
}

}
