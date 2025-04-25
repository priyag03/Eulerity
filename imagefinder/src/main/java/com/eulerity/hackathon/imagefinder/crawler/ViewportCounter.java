package com.eulerity.hackathon.imagefinder.crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ViewportCounter {

    public static void main(String[] args) throws InterruptedException {
        String url = (args.length > 0) ? args[0] : "https://www.emirates.com/us/english/";
        System.out.println("\uD83D\uDCCD Starting viewport measurement for: " + url);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Actions actions = new Actions(driver);
            int scrollCount = 1;
            int minScrolls = 5; // minimum number of scrolls before checking to stop

            Thread.sleep(4000);

            try {
                WebElement pressHold = driver.findElement(By.xpath("//button[contains(., 'PRESS & HOLD')]"));
                System.out.println("\uD83D\uDD10 Bot challenge detected. Attempting to press & hold...");
                actions.clickAndHold(pressHold).pause(5000).release().perform();
                Thread.sleep(5000);
            } catch (NoSuchElementException ignored) {
                System.out.println("✅ No bot challenge detected.");
            }

            long scrollTop = 0;
            long scrollHeight = ((Number) js.executeScript("return document.documentElement.scrollHeight")).longValue();
            long viewportHeight = ((Number) js.executeScript("return window.innerHeight")).longValue();
            long previousScrollTop = -1;

            new File("screenshots").mkdir();

            while (true) {
                System.out.printf("\uD83D\uDE9A Viewport #%d | Viewport Height: %d | Total Page Height: %d\n",
                        scrollCount, viewportHeight, scrollHeight);

                String timestamp = new SimpleDateFormat("HHmmss_SSS").format(new Date());
                String folderName = "screenshots/viewport_" + timestamp;
                File scrollFolder = new File(folderName);
                scrollFolder.mkdirs();

                // 📸 Save screenshot
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                BufferedImage image = ImageIO.read(screenshot);
                File screenshotFile = new File(scrollFolder, "screenshot.png");
                ImageIO.write(image, "png", screenshotFile);
                System.out.println("📸 Saved screenshot: " + screenshotFile.getAbsolutePath());

                // 📄 Save CSV of visible elements
                File csvFile = new File(scrollFolder, "elements.csv");
                try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                    writer.println("XPath,Tag,Text");

                    List<WebElement> elements = driver.findElements(By.xpath("//*"));
                    for (WebElement el : elements) {
                        try {
                            if (!el.isDisplayed()) continue;

                            String xpath = getXPath(el, driver);
                            String tag = el.getTagName();
                            String text = el.getText().replaceAll("[\\r\\n]+", " ").trim().replaceAll(",", " ");

                            writer.printf("\"%s\",\"%s\",\"%s\"%n", xpath, tag, text);
                        } catch (Exception ex) {
                            // Skip problematic elements
                        }
                    }
                    System.out.println("📄 Saved element CSV: " + csvFile.getAbsolutePath());
                }

                // Scroll down
                js.executeScript("window.scrollBy(0, arguments[0]);", viewportHeight);
                Thread.sleep(3000);

                // Lazy-load trigger
                js.executeScript("window.scrollBy(0, -100);");
                Thread.sleep(1000);
                js.executeScript("window.scrollBy(0, 100);");
                Thread.sleep(2000);

                // Update scroll info
                scrollTop = ((Number) js.executeScript("return window.scrollY")).longValue();
                scrollHeight = ((Number) js.executeScript("return document.documentElement.scrollHeight")).longValue();

                // ⛔ Check if stuck (scroll position didn't change) after minimum scrolls
                if (scrollCount >= minScrolls && scrollTop == previousScrollTop) {
                    System.out.println("✅ No more scrolling possible. Reached end of page.");
                    break;
                }

                previousScrollTop = scrollTop;
                scrollCount++;
            }

            System.out.printf("\uD83D\uDCCA Total viewports scrolled: %d\n", scrollCount);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // 📍 Helper method to get XPath
    private static String getXPath(WebElement element, WebDriver driver) {
        return (String) ((JavascriptExecutor) driver).executeScript(
            "function absoluteXPath(element) {" +
            "  var comp, comps = [];" +
            "  var parent = null;" +
            "  var xpath = '';" +
            "  var getPos = function(element) {" +
            "    var position = 1, curNode;" +
            "    for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {" +
            "      if (curNode.nodeName == element.nodeName) ++position;" +
            "    }" +
            "    return position;" +
            "  };" +
            "  for (; element && element.nodeType === 1; element = element.parentNode) {" +
            "    comp = {};" +
            "    comp.name = element.nodeName;" +
            "    comp.position = getPos(element);" +
            "    comps.push(comp);" +
            "  }" +
            "  for (var i = comps.length - 1; i >= 0; i--) {" +
            "    comp = comps[i];" +
            "    xpath += '/' + comp.name.toLowerCase();" +
            "    if (comp.position > 1) xpath += '[' + comp.position + ']';" +
            "  }" +
            "  return xpath;" +
            "} return absoluteXPath(arguments[0]);", element);
    }
}
