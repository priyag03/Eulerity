package com.eulerity.hackathon.imagefinder.crawler;

import java.util.Set;

public interface WebCrawler {
    void crawl(String startUrl, int maxSubpages, Set<String> collectedImages);
}
