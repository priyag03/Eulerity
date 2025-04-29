/*package com.eulerity.hackathon.imagefinder;

import com.microsoft.playwright.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.URI;
import java.net.URISyntaxException;

public class TestPlaywrightCrawler {

    public static void main(String[] args) {
        String startUrl = (args.length > 0) ? args[0] : "https://www.walmart.com/";
        System.out.println("Starting crawl for: " + startUrl);

        Set<String> allImageUrls = Collections.synchronizedSet(new HashSet<>());
        Set<String> crawledUrls = Collections.synchronizedSet(new HashSet<>());
        List<String> subpages = new ArrayList<>();

        // Crawl the main page first
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(startUrl);
            page.waitForLoadState();

            // Scroll for lazy loading
            for (int i = 0; i < 3; i++) {
                page.evaluate("window.scrollBy(0, window.innerHeight)");
                page.waitForTimeout(2000);
            }

            // Collect subpages after crawling the main page
            subpages = collectSameDomainSubpages(page, startUrl);
            crawledUrls.add(startUrl);  // Mark the main page as crawled
            browser.close();
        } catch (Exception e) {
            System.out.println("Failed to crawl start page: " + e.getMessage());
        }

        System.out.println("Found " + subpages.size() + " subpages to crawl.");

        // Crawl subpages only after the main page is done
        ExecutorService executor = Executors.newFixedThreadPool(5); // 5 threads

        // Crawl the main page first (if not already done)
        crawlAndCollectImages(startUrl, allImageUrls, crawledUrls);

        // Crawl the subpages in sequence
        for (String url : subpages) {
            if (!crawledUrls.contains(url)) {
                crawledUrls.add(url);
                executor.submit(() -> {
                    crawlAndCollectImages(url, allImageUrls, crawledUrls);
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n===================================");
        System.out.println("✅ TOTAL UNIQUE IMAGES FOUND: " + allImageUrls.size());
        System.out.println("===================================");
    }

    private static void crawlAndCollectImages(String url, Set<String> allImageUrls, Set<String> crawledUrls) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(url);
            page.waitForLoadState();

            Set<String> images = new HashSet<>();

            // Collect image sources from <img> tags
            for (ElementHandle img : page.querySelectorAll("img")) {
                String src = img.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    images.add(makeAbsoluteUrl(url, src));
                }
                String srcset = img.getAttribute("srcset");
                if (srcset != null && !srcset.isEmpty()) {
                    images.addAll(Arrays.asList(srcset.split(",")));
                }
            }

            // Collect background images from inline styles
            for (ElementHandle div : page.querySelectorAll("div")) {
                String style = div.getAttribute("style");
                if (style != null && style.contains("background-image")) {
                    String bgUrl = extractBackgroundImageUrl(style);
                    if (bgUrl != null) {
                        images.add(makeAbsoluteUrl(url, bgUrl));
                    }
                }
            }

            allImageUrls.addAll(images);

            System.out.println("✅ Crawled URL: " + url + " | Images Found: " + images.size());

            browser.close();
        } catch (Exception e) {
            System.out.println("❌ Failed to crawl: " + url);
            e.printStackTrace();
        }
    }

    private static List<String> collectSameDomainSubpages(Page page, String startUrl) {
        Set<String> subpages = new HashSet<>();
        String startingDomain = null;

        try {
            startingDomain = getDomain(startUrl);  // This may throw URISyntaxException
        } catch (URISyntaxException e) {
            System.out.println("Invalid start URL: " + startUrl);
            e.printStackTrace();
            return new ArrayList<>(subpages); // Return early if URL is invalid
        }

        for (ElementHandle aElement : page.querySelectorAll("a")) {
            String href = aElement.getAttribute("href");
            if (href != null && !href.isEmpty() && isValidHref(href)) {
                String absoluteUrl = makeAbsoluteUrl(startUrl, href);
                String linkDomain = null;

                try {
                    linkDomain = getDomain(absoluteUrl);  // This may throw URISyntaxException
                } catch (URISyntaxException e) {
                    System.out.println("Invalid URL: " + absoluteUrl);
                    continue;  // Skip this URL if there's an error
                }

                if (linkDomain != null && linkDomain.equals(startingDomain)) {
                    subpages.add(absoluteUrl);

                    // 🛑 Limit to 10 subpages
                    if (subpages.size() >= 30) {
                        break;
                    }
                }
            }
        }

        System.out.println("Found " + subpages.size() + " subpages to crawl.");
        return new ArrayList<>(subpages);
    }

    private static String getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }

    private static boolean isValidHref(String href) {
        href = href.toLowerCase();
        return !(href.startsWith("javascript:") || href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("#"));
    }

    private static String makeAbsoluteUrl(String baseUrl, String href) {
        try {
            URI base = new URI(baseUrl);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            return href;
        }
    }

    private static String extractBackgroundImageUrl(String style) {
        try {
            int start = style.indexOf("url(");
            int end = style.indexOf(")", start);
            if (start != -1 && end != -1) {
                return style.substring(start + 4, end).replaceAll("['\"]", "");
            }
        } catch (Exception e) {
            // ignored
        }
        return null;
    }
}

/////// Final Working versionnnnn  */


/*package com.eulerity.hackathon.imagefinder.utils;

import com.microsoft.playwright.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SubdomainCrawler {

    public static Set<String> crawlPaths(String startUrl) {
        System.out.println("Executed SUbDOmian Crawler");
        Set<String> discoveredPaths = new HashSet<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(startUrl);
            page.waitForLoadState();

            String baseDomain;
            try {
                baseDomain = getBaseDomain(startUrl);
            } catch (URISyntaxException e) {
                System.err.println("Invalid start URL: " + startUrl);
                return discoveredPaths;
            }

            Set<String> hrefs = new HashSet<>((List<String>) page.evalOnSelectorAll("a[href]", "els => els.map(e => e.href)"));

            for (String href : hrefs) {
                try {
                    URI uri = new URI(href);
                    String host = uri.getHost();
                    if (host != null && normalizeHost(host).equals(baseDomain)) {
                        String path = uri.getPath();
                        if (path != null && !path.isEmpty()) {
                            discoveredPaths.add(path);
                        }
                    }
                } catch (URISyntaxException e) {
                    System.err.println("Skipping invalid URL: " + href);
                }
            }

            browser.close();
        }

        return discoveredPaths;
    }

    private static String getBaseDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String host = uri.getHost();
        String[] parts = host.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
        return host;
    }

    private static String normalizeHost(String host) {
        return (host != null && host.startsWith("www.")) ? host.substring(4) : host;
    }

    public static void main(String[] args) {
        String inputUrl = (args.length > 0) ? args[0] : "https://www.emirates.com";
        System.out.println("🔍 Crawling paths for domain: " + inputUrl);
        Set<String> result = crawlPaths(inputUrl);
        System.out.println("\n✅ Found " + result.size() + " unique path(s):");
        result.stream().sorted().forEach(path -> System.out.println(" - " + path));
    }
}
*/