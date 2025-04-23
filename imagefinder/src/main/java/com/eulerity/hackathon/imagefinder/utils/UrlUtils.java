package com.eulerity.hackathon.imagefinder.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    // ✅ Check if candidate URL belongs to the same domain as the root
    public static boolean isSameDomain(String root, String candidate) {
        try {
            URL rootUrl = new URL(root);
            URL candidateUrl = new URL(candidate);

            return rootUrl.getHost().equalsIgnoreCase(candidateUrl.getHost());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    // ✅ Normalize URL (remove fragment, trailing slashes, etc.)
    public static String normalize(String url) {
        try {
            URL parsed = new URL(url);
            return new URL(
                parsed.getProtocol(),
                parsed.getHost(),
                parsed.getPort(),
                parsed.getPath() // no query or fragment
            ).toString();
        } catch (MalformedURLException e) {
            return url;
        }
    }
}
