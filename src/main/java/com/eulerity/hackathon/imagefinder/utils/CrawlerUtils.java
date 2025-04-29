package com.eulerity.hackathon.imagefinder.util;

import java.net.URI;
import java.net.URISyntaxException;

public class CrawlerUtils {

    public static String getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }

    public static boolean isValidHref(String href) {
        href = href.toLowerCase();
        return !(href.startsWith("javascript:") || href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("#"));
    }

    public static String makeAbsoluteUrl(String baseUrl, String href) {
        try {
            URI base = new URI(baseUrl);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            return href;
        }
    }

    public static String extractBackgroundImageUrl(String style) {
        try {
            int start = style.indexOf("url(");
            int end = style.indexOf(")", start);
            if (start != -1 && end != -1) {
                return style.substring(start + 4, end).replaceAll("['\"]", "");
            }
        } catch (Exception e) {
        
        }
        return null;
    }
}
