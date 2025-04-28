package com.eulerity.hackathon.imagefinder.websocket;

import com.eulerity.hackathon.imagefinder.crawler.PlaywrightCrawler;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper; // 🛑 Add Jackson JSON parser

@ServerEndpoint("/crawl-socket")
public class CrawlWebSocketServer {

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("🟢 New WebSocket connection: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("🔴 WebSocket closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        System.out.println("❌ WebSocket error: " + session.getId() + " - " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("📩 Message received from client: " + message);

        try {
            // 🛑 Parse the incoming JSON message
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(message, Map.class);

            String startUrl = (String) data.get("startUrl");
            int subpages = (int) data.get("subpages");

            // 🛑 Start crawling in a new Thread
            new Thread(() -> {
                PlaywrightCrawler crawler = new PlaywrightCrawler();
                crawler.crawl(startUrl, subpages, new HashSet<>());
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcastImage(String imageUrl) {
    for (Session session : sessions) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(imageUrl); // ✅ Non-blocking, safe
        }
    }
}

}
