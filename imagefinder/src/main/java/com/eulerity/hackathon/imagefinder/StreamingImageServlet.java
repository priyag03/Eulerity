package com.eulerity.hackathon.imagefinder;

import com.eulerity.hackathon.imagefinder.crawler.ScrollCrawler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "StreamingImageServlet", urlPatterns = {"/stream"})
public class StreamingImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getParameter("url");
        if (url == null || url.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'url' parameter");
            return;
        }

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        ScrollCrawler.crawlWithStreaming(url, imageData -> {
            try {
                String json = "{ \"url\": \"" + imageData.get("url") + "\", \"xpath\": \"" + imageData.get("xpath") + "\" }";
                out.write("data: " + json + "\n\n");
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        out.close(); // End of event stream
    }
}
