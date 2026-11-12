package com.interview.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class LocalApiServer implements AutoCloseable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpServer server;
    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();
    private static final String TOKEN = "practice-token";
    private static final Map<String, BigDecimal> PRICES = Map.of(
            "p-100", new BigDecimal("12.50"), "p-200", new BigDecimal("7.00"));

    public LocalApiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }
    @Override public void close() { server.stop(0); }

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            if (path.equals("/api/auth/login") && method.equals("POST")) {
                JsonNode body = readBody(ex);
                if ("qa".equals(body.path("username").asText()) && "secret".equals(body.path("password").asText())) {
                    send(ex, 200, Map.of("token", TOKEN, "expiresIn", 3600));
                } else {
                    error(ex, 401, "INVALID_CREDENTIALS");
                }
                return;
            }
            if (!("Bearer " + TOKEN).equals(ex.getRequestHeaders().getFirst("Authorization"))) {
                error(ex, 401, "UNAUTHORIZED");
                return;
            }
            if (path.equals("/api/products") && method.equals("GET")) {
                send(ex, 200, Map.of("products", new Object[]{
                        Map.of("id", "p-100", "name", "Keyboard", "price", new BigDecimal("12.50"), "stock", 25),
                        Map.of("id", "p-200", "name", "Mouse", "price", new BigDecimal("7.00"), "stock", 50)}));
                return;
            }
            if (path.equals("/api/orders") && method.equals("POST")) {
                JsonNode body = readBody(ex);
                String productId = body.path("productId").asText();
                if (!PRICES.containsKey(productId)) {
                    error(ex, 404, "PRODUCT_NOT_FOUND");
                    return;
                }
                int quantity = body.path("quantity").asInt(0);
                if (quantity < 1 || quantity > 5) {
                    error(ex, 400, "INVALID_QUANTITY");
                    return;
                }
                String orderId = "ord-" + UUID.randomUUID();
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("orderId", orderId);
                order.put("productId", productId);
                order.put("quantity", quantity);
                order.put("status", "CREATED");
                order.put("total", PRICES.get(productId).multiply(BigDecimal.valueOf(quantity)));
                orders.put(orderId, order);
                ex.getResponseHeaders().add("Location", "/api/orders/" + orderId);
                send(ex, 201, order);
                return;
            }
            if (path.startsWith("/api/orders/") && method.equals("GET")) {
                String id = path.substring("/api/orders/".length());
                if (!orders.containsKey(id)) { error(ex, 404, "ORDER_NOT_FOUND"); return; }
                send(ex, 200, orders.get(id));
                return;
            }
            error(ex, 404, "NOT_FOUND");
        } catch (Exception e) {
            error(ex, 500, "SERVER_ERROR");
        } finally {
            ex.close();
        }
    }

    private JsonNode readBody(HttpExchange ex) throws IOException {
        return mapper.readTree(ex.getRequestBody());
    }
    private void error(HttpExchange ex, int status, String code) throws IOException {
        send(ex, status, Map.of("error", Map.of("code", code, "message", code.replace('_', ' '))));
    }
    private void send(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (var out = ex.getResponseBody()) { out.write(bytes); }
    }
}
