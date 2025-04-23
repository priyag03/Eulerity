package com.eulerity.hackathon.imagefinder.crawler;

import com.eulerity.hackathon.imagefinder.extractor.ImageScrollExtractor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.function.Consumer;

import java.util.*;

public class ScrollCrawler {

    public static List<Map<String, String>> crawlWithXPaths(String url) {
        System.out.println("? Starting crawl with XPath: " + url);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(options);

        List<Map<String, String>> imagesWithXPaths = new ArrayList<>();

        try {
            driver.get(url);
            JavascriptExecutor js = (JavascriptExecutor) driver;

           // Find the largest scrollable container
Object scrollTarget = js.executeScript(
    "let scrollables = Array.from(document.querySelectorAll('*')).filter(e => e.scrollHeight > e.clientHeight);" +
    "if (scrollables.length === 0) return document.scrollingElement;" +
    "scrollables.sort((a, b) => b.scrollHeight - a.scrollHeight);" +
    "return scrollables[0];"
);

long lastHeight = ((Number) js.executeScript("return arguments[0].scrollHeight;", scrollTarget)).longValue();
int scrollCount = 1;

while (true) {
    System.out.println("? Scrolling... viewport #" + scrollCount++);

    // Scroll to bottom of scrollable element
    js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollTarget);
    Thread.sleep(4000);

    // Jiggle scroll to trigger lazy-load
    js.executeScript("arguments[0].scrollBy(0, -300);", scrollTarget);
    Thread.sleep(4000);
    js.executeScript("arguments[0].scrollBy(0, 300);", scrollTarget);
    Thread.sleep(4000);

    // Extract image data
    imagesWithXPaths.addAll(ImageScrollExtractor.extractAllWithXPaths(driver));

    long newHeight = ((Number) js.executeScript("return arguments[0].scrollHeight;", scrollTarget)).longValue();
    int stagnantScrolls = 0;
if (newHeight == lastHeight) {
    stagnantScrolls++;
    if (stagnantScrolls >= 3) {
        break;
    }
} else {
    stagnantScrolls = 0;
}

    lastHeight = newHeight;
}


            System.out.println("? Images found: " + imagesWithXPaths.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return imagesWithXPaths;
    }


public static void crawlWithStreaming(String url, Consumer<Map<String, String>> imageCallback) {
    System.out.println("📡 Streaming crawl started for: " + url);

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox");
    WebDriver driver = new ChromeDriver(options);

    Set<String> seenUrls = new HashSet<>();

    try {
        driver.get(url);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Object scrollTarget = js.executeScript(
            "let scrollables = Array.from(document.querySelectorAll('*')).filter(e => e.scrollHeight > e.clientHeight);" +
            "if (scrollables.length === 0) return document.scrollingElement;" +
            "scrollables.sort((a, b) => b.scrollHeight - a.scrollHeight);" +
            "return scrollables[0];"
        );

        long lastHeight = ((Number) js.executeScript("return arguments[0].scrollHeight;", scrollTarget)).longValue();
        int scrollCount = 1;
        int stagnantScrolls = 0;

        while (stagnantScrolls < 5) {
            System.out.println("⬇️ Viewport #" + scrollCount++);

            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollTarget);
            Thread.sleep(2000);
            js.executeScript("arguments[0].scrollBy(0, -200);", scrollTarget);
            Thread.sleep(1000);
            js.executeScript("arguments[0].scrollBy(0, 300);", scrollTarget);
            Thread.sleep(1500);

            List<Map<String, String>> batch = ImageScrollExtractor.extractAllWithXPaths(driver);
            int newFound = 0;

            for (Map<String, String> image : batch) {
                String urlStr = image.get("url");
                if (urlStr != null && seenUrls.add(urlStr)) {
                    imageCallback.accept(image); // 🔥 stream it
                    newFound++;
                }
            }

            System.out.println("🖼️ New streamed: " + newFound);

            long newHeight = ((Number) js.executeScript("return arguments[0].scrollHeight;", scrollTarget)).longValue();
            if (newHeight == lastHeight && newFound == 0) {
                stagnantScrolls++;
            } else {
                stagnantScrolls = 0;
            }
            lastHeight = newHeight;
        }

        System.out.println("✅ Stream crawl complete.");

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        driver.quit();
    }
}

}
