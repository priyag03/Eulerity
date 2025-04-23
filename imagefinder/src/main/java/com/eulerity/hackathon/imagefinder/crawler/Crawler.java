package com.eulerity.hackathon.imagefinder.crawler;

import com.eulerity.hackathon.imagefinder.extractor.ImageExtractor;
import com.eulerity.hackathon.imagefinder.utils.UrlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private final ExecutorService executor;
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Set<String> resultImages = ConcurrentHashMap.newKeySet();

    public Crawler(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public List<String> crawl(String rootUrl) {
        // Kick off with the first URL
        visited.add(rootUrl);
        executor.submit(new CrawlTask(rootUrl, rootUrl));

        // Wait for a short time for threads to complete (simple version)
        executor.shutdown();
        try {
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(resultImages);
    }

    // Inner class for individual crawl tasks
    private class CrawlTask implements Runnable {
        private final String url;
        private final String rootDomain;

        public CrawlTask(String url, String rootDomain) {
            this.url = url;
            this.rootDomain = rootDomain;
        }

@Override
public void run() {
    try {
        Document doc = Jsoup.connect(url)
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
    .timeout(10000)
    .get();
// ✅ Print the HTML
System.out.println("🌐 HTML for: " + url);
System.out.println(doc.html().substring(0, Math.min(3000, doc.html().length())));

// ✅ Count image-related tags
int imgCount = doc.select("img").size();
int sourceCount = doc.select("source").size();
int pictureCount = doc.select("picture").size(); // optional

int totalImageTags = imgCount + sourceCount + pictureCount;

System.out.println("🖼️ Total image-related tags found: " + totalImageTags);
System.out.println(" - <img>: " + imgCount);
System.out.println(" - <source>: " + sourceCount);
System.out.println(" - <picture>: " + pictureCount);

// ✅ Print all image src values
doc.select("img").forEach(img -> {
    String src = img.absUrl("src");
    if (!src.isEmpty()) {
        System.out.println(" → " + src);
    }
});


        // 🔍 Step 1: Print current URL
        System.out.println("🧭 Visiting: " + url);

        // 🔍 Step 2: Print raw HTML
        System.out.println("📄 HTML content (first 1000 chars):");
        System.out.println(doc.html().substring(0, Math.min(1000, doc.html().length())));

        // 🔍 Step 3: Extract images
        resultImages.addAll(ImageExtractor.extractImages(doc));

        // 🔍 Step 4: Find sub-links
        Elements links = doc.select("a[href]");
        links.forEach(link -> {
            String absHref = link.absUrl("href");
            String normalized = UrlUtils.normalize(absHref);

            if (UrlUtils.isSameDomain(rootDomain, normalized) && visited.add(normalized)) {
                executor.submit(new CrawlTask(normalized, rootDomain));
            }
        });

    } catch (IOException e) {
        System.err.println("❌ Failed to crawl: " + url);
        e.printStackTrace();
    }
}

    }
}

