package com.eulerity.hackathon.imagefinder.extractor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageExtractor {

    public static List<String> extractImages(Document doc) {
        Set<String> imageUrls = new LinkedHashSet<>();

        // ✅ Extract from <img> tags
        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            String src = img.absUrl("src");
            String srcset = img.attr("srcset");

            if (srcset != null && !srcset.isEmpty()) {
                String bestSrc = extractHighestResFromSrcset(srcset);
                if (!bestSrc.isEmpty()) imageUrls.add(bestSrc);
            }

            if (src != null && !src.isEmpty() && isImageUrl(src)) {
                imageUrls.add(src);
            }
        }

        // 🔍 Extract background-image from inline styles
Elements styledDivs = doc.select("*[style]");
for (Element div : styledDivs) {
    String style = div.attr("style");
    if (style.contains("background-image")) {
        Matcher matcher = Pattern.compile("url\\(['\"]?(.*?)['\"]?\\)").matcher(style);
        if (matcher.find()) {
            String bgUrl = matcher.group(1);
            if (!bgUrl.startsWith("http")) {
                bgUrl = div.baseUri() + bgUrl;
            }
            if (isImageUrl(bgUrl)) {
                imageUrls.add(bgUrl);
            }
        }
    }
}
// 🔍 Look for <a><img> patterns
Elements anchors = doc.select("a img");
for (Element img : anchors) {
    String src = img.absUrl("src");
    if (src != null && !src.isEmpty() && isImageUrl(src)) {
        imageUrls.add(src);
    }
}


        // ✅ Extract from <source> elements
        Elements sources = doc.select("source");
        for (Element source : sources) {
            String src = source.absUrl("src");
            String srcset = source.attr("srcset");

            if (srcset != null && !srcset.isEmpty()) {
                String bestSrc = extractHighestResFromSrcset(srcset);
                if (!bestSrc.isEmpty()) imageUrls.add(bestSrc);
            }

            if (!src.isEmpty() && isImageUrl(src)) {
                imageUrls.add(src);
            }
        }

        return new ArrayList<>(imageUrls);
    }

    // 👇 Extract the highest resolution image from srcset
    private static String extractHighestResFromSrcset(String srcset) {
        String[] candidates = srcset.split(",");
        String bestUrl = "";
        int maxWidth = -1;

        Pattern pattern = Pattern.compile("(.+?)\\s+(\\d+)w");

        for (String candidate : candidates) {
            Matcher matcher = pattern.matcher(candidate.trim());
            if (matcher.matches()) {
                String url = matcher.group(1).trim();
                int width = Integer.parseInt(matcher.group(2));
                if (width > maxWidth) {
                    maxWidth = width;
                    bestUrl = url;
                }
            }
        }

        return bestUrl;
    }

    private static boolean isImageUrl(String url) {
        return url.matches("(?i).+\\.(png|jpe?g|gif|bmp|webp|svg|avif)(\\?.*)?$");
    }
}
