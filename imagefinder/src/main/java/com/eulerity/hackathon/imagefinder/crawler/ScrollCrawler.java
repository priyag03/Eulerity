package com.eulerity.hackathon.imagefinder.crawler;

import com.eulerity.hackathon.imagefinder.extractor.ImageScrollExtractor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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

            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
            int scrollCount = 1;

            while (true) {
                System.out.println("? Scrolling... viewport #" + scrollCount++);

                // Scroll to bottom
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000);

                // Jiggle scroll to trigger lazy-loading
                js.executeScript("window.scrollBy(0, -150);");
                Thread.sleep(1000);
                js.executeScript("window.scrollBy(0, 300);");
                Thread.sleep(2000);

                // Extract image data after scroll
                imagesWithXPaths.addAll(ImageScrollExtractor.extractAllWithXPaths(driver));

                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    System.out.println("? Reached bottom of the page.");
                    break;
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
}
