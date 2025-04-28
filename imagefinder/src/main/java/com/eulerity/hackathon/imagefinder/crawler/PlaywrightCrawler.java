package com.eulerity.hackathon.imagefinder.crawler;

import com.microsoft.playwright.*;
import com.eulerity.hackathon.imagefinder.extractor.SubpageExtractor;
import com.eulerity.hackathon.imagefinder.util.CrawlerUtils;
import com.eulerity.hackathon.imagefinder.websocket.CrawlWebSocketServer; // 🛑 ADD THIS

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlaywrightCrawler implements WebCrawler {

    @Override
    public void crawl(String startUrl, int maxSubpages, Set<String> collectedImages) {
        System.out.println("Started Executing PlaywrightCrawler");
        Set<String> crawledUrls = Collections.synchronizedSet(new HashSet<String>());
        List<String> subpages = new ArrayList<String>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(startUrl);
            page.waitForLoadState();

            scrollPage(page);

            // 🛑 Collect images immediately after scrolling
            crawlPageUsingSession(page, startUrl, collectedImages);
            crawledUrls.add(startUrl);

            // 🛑 Collect subpages after images
            subpages = SubpageExtractor.collectSubpages(page, startUrl, maxSubpages);

            // 🛑 Crawl subpages
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(5, maxSubpages));

            for (final String url : subpages) {
                if (!crawledUrls.contains(url)) {
                    crawledUrls.add(url);
                    executor.submit(new Runnable() {
                        public void run() {
                            crawlPage(url, collectedImages, crawledUrls);
                        }
                    });
                }
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.MINUTES);

            browser.close();
        } catch (Exception e) {
            System.out.println("Failed to crawl: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n===================================");
        System.out.println("✅ TOTAL UNIQUE IMAGES FOUND: " + collectedImages.size());
        System.out.println("===================================");
    }

    // 🛑 Crawl Main Page Using Same Session
    private void crawlPageUsingSession(Page page, String url, Set<String> allImageUrls) {
        try {
            Set<String> images = collectImagesFromPage(page, url);
            allImageUrls.addAll(images);
            System.out.println("✅ Crawled START URL: " + url + " | Images Found: " + images.size());
        } catch (Exception e) {
            System.out.println("❌ Failed to crawl start page: " + url);
            e.printStackTrace();
        }
    }

    // 🛑 Crawl Subpages in New Browser (separate)
    private void crawlPage(String url, Set<String> allImageUrls, Set<String> crawledUrls) {
        long startTime = System.currentTimeMillis();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(url);
            page.waitForLoadState();

            scrollPage(page); // scroll subpage also!

            Set<String> images = collectImagesFromPage(page, url);
            allImageUrls.addAll(images);

            System.out.println("✅ Crawled SUBPAGE URL: " + url + " | Images Found: " + images.size());

            browser.close();
        } catch (Exception e) {
            System.out.println("❌ Failed to crawl subpage: " + url);
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); // 🕒 End timing
long timeTakenMs = endTime - startTime;
System.out.println("⏱️ Time taken to crawl URL: " + url + " = " + timeTakenMs + " ms (" + (timeTakenMs / 1000.0) + " seconds)");

    }

    // 🛑 Scroll for lazy loading
    private void scrollPage(Page page) {
        int maxScrolls = 30;
        for (int i = 0; i < maxScrolls; i++) {
            page.evaluate("window.scrollBy(0, window.innerHeight)");
            page.waitForTimeout(1500); // 1.5 seconds after each scroll
        }
        // 🛑 After scrolling fully, wait more to allow lazy images to load
        page.waitForTimeout(5000); // wait 5 seconds extra
    }

    // 🛑 Extract images from the current page
    private Set<String> collectImagesFromPage(Page page, String baseUrl) {
    Set<String> images = new HashSet<>();

    List<ElementHandle> imgElements = page.querySelectorAll("img");
    for (ElementHandle img : imgElements) {
        String src = img.getAttribute("src");
        if (src != null && !src.isEmpty()) {
            String absoluteUrl = CrawlerUtils.makeAbsoluteUrl(baseUrl, src);
            if (images.add(absoluteUrl)) { // ✅ Only broadcast if newly added
                CrawlWebSocketServer.broadcastImage(absoluteUrl);
            }
        }

        String srcset = img.getAttribute("srcset");
        if (srcset != null && !srcset.isEmpty()) {
            List<String> srcUrls = parseSrcset(baseUrl, srcset);
            for (String urlFromSrcset : srcUrls) {
                if (images.add(urlFromSrcset)) { // ✅ again only if new
                    CrawlWebSocketServer.broadcastImage(urlFromSrcset);
                }
            }
        }
    }

    // Similarly for <source> elements
    List<ElementHandle> sourceElements = page.querySelectorAll("source");
    for (ElementHandle source : sourceElements) {
        String srcset = source.getAttribute("srcset");
        if (srcset != null && !srcset.isEmpty()) {
            List<String> srcUrls = parseSrcset(baseUrl, srcset);
            for (String urlFromSrcset : srcUrls) {
                if (images.add(urlFromSrcset)) { // ✅
                    CrawlWebSocketServer.broadcastImage(urlFromSrcset);
                }
            }
        }
    }

    // Similarly for background images
    List<ElementHandle> divElements = page.querySelectorAll("div");
    for (ElementHandle div : divElements) {
        String style = div.getAttribute("style");
        if (style != null && style.contains("background-image")) {
            String bgUrl = CrawlerUtils.extractBackgroundImageUrl(style);
            if (bgUrl != null) {
                String absoluteUrl = CrawlerUtils.makeAbsoluteUrl(baseUrl, bgUrl);
                if (images.add(absoluteUrl)) { // ✅
                    CrawlWebSocketServer.broadcastImage(absoluteUrl);
                }
            }
        }
    }

    return images;
}


    // 🛑 Parse srcset cleanly
    private List<String> parseSrcset(String baseUrl, String srcset) {
        List<String> urls = new ArrayList<String>();
        String[] parts = srcset.split(",");
        for (String part : parts) {
            String[] items = part.trim().split(" ");
            if (items.length > 0) {
                urls.add(CrawlerUtils.makeAbsoluteUrl(baseUrl, items[0]));
            }
        }
        return urls;
    }
}
