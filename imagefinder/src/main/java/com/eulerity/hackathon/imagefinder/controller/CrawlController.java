package com.eulerity.hackathon.imagefinder.controller;

import com.eulerity.hackathon.imagefinder.manager.CrawlerManager;
import com.eulerity.hackathon.imagefinder.crawler.PlaywrightCrawler;
import org.json.JSONObject;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/crawl")
public class CrawlController extends HttpServlet {
   

    private CrawlerManager crawlerManager;

    @Override
    public void init() throws ServletException {
        super.init();
        crawlerManager = new CrawlerManager(new PlaywrightCrawler());
    }

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    StringBuilder jsonBuffer = new StringBuilder();
    String line;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
        jsonBuffer.append(line);
    }

    JSONObject requestBody = new JSONObject(jsonBuffer.toString());

    String startUrl = requestBody.getString("url");
    int maxSubpages = requestBody.getInt("subpages");

    response.setContentType("application/json; charset=UTF-8");
    JSONObject jsonResponse = new JSONObject();

    try {
        // 🛑 Collect images
        CrawlerManager manager = new CrawlerManager(new PlaywrightCrawler());
        Set<String> crawledImages = manager.startCrawlingAndReturnImages(startUrl, maxSubpages);

        jsonResponse.put("status", "success");
        jsonResponse.put("message", "✅ Crawling completed for: " + startUrl + " with " + crawledImages.size() + " images.");
        jsonResponse.put("images", crawledImages); // 🛑 include images list
    } catch (Exception e) {
        jsonResponse.put("status", "error");
        jsonResponse.put("message", "❌ Failed to crawl: " + e.getMessage());
    }

    response.getWriter().write(jsonResponse.toString());
}


}
