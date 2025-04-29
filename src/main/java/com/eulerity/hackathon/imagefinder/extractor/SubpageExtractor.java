package com.eulerity.hackathon.imagefinder.extractor;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.eulerity.hackathon.imagefinder.util.CrawlerUtils;

import java.util.*;

public class SubpageExtractor {
    
    public static List<String> collectSubpages(Page page, String baseUrl, int limit) {
        System.out.println("Started Executing SubPage Extractor");
        Set<String> subpages = new HashSet<String>();
        String baseDomain = null;

        try {
            baseDomain = CrawlerUtils.getDomain(baseUrl);
        } catch (Exception e) {
            System.out.println("Invalid start URL: " + baseUrl);
            e.printStackTrace();
            return new ArrayList<String>();
        }

        List<ElementHandle> links = page.querySelectorAll("a");
        for (ElementHandle link : links) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && CrawlerUtils.isValidHref(href)) {
                String absoluteUrl = CrawlerUtils.makeAbsoluteUrl(baseUrl, href);
                try {
                    String linkDomain = CrawlerUtils.getDomain(absoluteUrl);
                    if (linkDomain != null && linkDomain.equals(baseDomain)) {
                        subpages.add(absoluteUrl);
                        if (subpages.size() >= limit) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Invalid URL: " + absoluteUrl);
                    continue;
                }
            }
        }
        return new ArrayList<String>(subpages);
    }
}
