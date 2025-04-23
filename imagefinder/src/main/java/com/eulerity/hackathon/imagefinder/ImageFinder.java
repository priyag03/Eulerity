
package com.eulerity.hackathon.imagefinder;
import com.eulerity.hackathon.imagefinder.crawler.Crawler;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  };

	@Override
protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");

    String path = req.getServletPath();
    String url = req.getParameter("url");
    System.out.println("Got request to: " + path + " with query param: " + url);

    List<String> imageUrls;

try {
    Crawler crawler = new Crawler(10); // 10 threads
    imageUrls = crawler.crawl(url);   // crawl all images
} catch (Exception e) {
    imageUrls = new ArrayList<>();    // ✅ Java 8-friendly fallback
    e.printStackTrace();
}

    // ✅ Respond with JSON
    resp.getWriter().print(GSON.toJson(imageUrls));
}

}
