package com.eulerity.hackathon.imagefinder;

import com.eulerity.hackathon.imagefinder.crawler.ScrollCrawler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String path = req.getServletPath();
        String url = req.getParameter("url");
        System.out.println("Got request to: " + path + " with query param: " + url);

        List<Map<String, String>> imagesWithXPaths;
        try {
            imagesWithXPaths = ScrollCrawler.crawlWithXPaths(url);
        } catch (Exception e) {
            imagesWithXPaths = new ArrayList<>();
            e.printStackTrace();
        }

        resp.getWriter().print(GSON.toJson(imagesWithXPaths));
    }
}
