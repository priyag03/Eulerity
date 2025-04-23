package com.eulerity.hackathon.imagefinder.extractor;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public class ImageScrollExtractor {

    public static List<Map<String, String>> extractAllWithXPaths(WebDriver driver) {
        List<Map<String, String>> imagesWithXPaths = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.switchTo().defaultContent(); // ensure not inside an iframe
        } catch (Exception e) {
            System.err.println("⚠️ Error switching to default content.");
        }

        // Collect only target elements: img, source, or any with style or computed background-image
        List<WebElement> candidates = new ArrayList<>();
        candidates.addAll(driver.findElements(By.tagName("img")));
        candidates.addAll(driver.findElements(By.tagName("source")));
        candidates.addAll(driver.findElements(By.xpath("//*[contains(@style,'background')]")));

        for (WebElement element : candidates) {
            try {
                String tag = element.getTagName().toLowerCase();
                String src = null;

               if (tag.equals("img")) {
    src = element.getAttribute("src");

    if ((src == null || src.isEmpty()) && element.getAttribute("srcset") != null) {
        // srcset may contain: "url1 1x, url2 2x"
        String[] srcsetParts = element.getAttribute("srcset").split(",");
        if (srcsetParts.length > 0) {
            src = srcsetParts[0].trim().split(" ")[0];  // take first URL
        }
    }

    // Fallbacks
    if (src == null || src.isEmpty()) src = element.getAttribute("data-src");
    if (src == null || src.isEmpty()) src = element.getAttribute("data-srcset");
    if (src == null || src.isEmpty()) src = element.getAttribute("data-image-src");
}
System.out.println("Tag: " + tag + ", src: " + src);



                // Case 2: <source srcset="...">
                if (tag.equals("source")) {
                    src = element.getAttribute("srcset");
                }

                // Case 3: inline background-image: url(...)
                String style = element.getAttribute("style");
                if (style != null && style.contains("background-image")) {
                    int start = style.indexOf("url(") + 4;
                    int end = style.indexOf(")", start);
                    if (start > 3 && end > start) {
                        src = style.substring(start, end).replace("\"", "");
                    }
                }

                // Case 4: computed background-image using JS
                String computedStyle = (String) js.executeScript(
                        "return window.getComputedStyle(arguments[0]).backgroundImage;", element);
                if (computedStyle != null && computedStyle.contains("url(")) {
                    int start = computedStyle.indexOf("url(") + 4;
                    int end = computedStyle.indexOf(")", start);
                    if (start > 3 && end > start) {
                        src = computedStyle.substring(start, end).replace("\"", "");
                    }
                }

                if (src != null && !src.trim().isEmpty() && !src.equals("none")) {
                    Map<String, String> imageData = new HashMap<>();
                    imageData.put("url", src);
                    imageData.put("xpath", getAbsoluteXPath(driver, element));
                    imagesWithXPaths.add(imageData);
                }
            } catch (Exception ex) {
                System.err.println("❌ Skipping element due to: " + ex.getMessage());
            }
        }

        return imagesWithXPaths;
    }

    private static String getAbsoluteXPath(WebDriver driver, WebElement element) {
        return (String) ((JavascriptExecutor) driver).executeScript(
                "function absoluteXPath(el) {" +
                        "var xpath = '';" +
                        "for (; el && el.nodeType == 1; el = el.parentNode) {" +
                        "idx = 1;" +
                        "for (var sib = el.previousSibling; sib; sib = sib.previousSibling) {" +
                        "if (sib.nodeType == 1 && sib.nodeName == el.nodeName) idx++;" +
                        "}" +
                        "xpath = '/' + el.nodeName.toLowerCase() + '[' + idx + ']' + xpath;" +
                        "}" +
                        "return xpath;" +
                        "}" +
                        "return absoluteXPath(arguments[0]);", element);
    }
}
