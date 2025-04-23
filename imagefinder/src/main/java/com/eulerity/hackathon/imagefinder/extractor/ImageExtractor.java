package com.eulerity.hackathon.imagefinder.extractor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ImageExtractor {

    public static List<String> extractImages(Document doc) {
        List<String> imageUrls = new ArrayList<>();

        // Select all <img> tags
        Elements images = doc.select("img");

        for (Element img : images) {
            // Get the absolute URL of src attribute
            String src = img.absUrl("src");

            // Only add if it's non-empty and a valid image
            if (src != null && !src.isEmpty() && isImageUrl(src)) {
                imageUrls.add(src);
            }
        }

        return imageUrls;
    }

    // Optional: basic check if the URL looks like an image
    private static boolean isImageUrl(String url) {
        return url.matches("(?i).+\\.(png|jpe?g|gif|bmp|webp|svg)");
    }
}
