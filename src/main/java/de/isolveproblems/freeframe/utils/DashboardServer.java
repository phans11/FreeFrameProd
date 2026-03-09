package de.isolveproblems.freeframe.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.isolveproblems.freeframe.FreeFrame;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DashboardServer {
    private final FreeFrame freeframe;
    private HttpServer server;

    public DashboardServer(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public synchronized void start() {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.dashboard.enabled", false)) {
            return;
        }
        if (this.server != null) {
            return;
        }

        int port = this.freeframe.getPluginConfig().getInt("freeframe.dashboard.port", 8095);
        String host = this.freeframe.getPluginConfig().getString("freeframe.dashboard.host", "127.0.0.1");
        try {
            this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
            this.server.createContext("/health", new JsonHandler(this, "health"));
            this.server.createContext("/stats", new JsonHandler(this, "stats"));
            this.server.createContext("/frames", new JsonHandler(this, "frames"));
            this.server.setExecutor(null);
            this.server.start();
            this.freeframe.getLogger().info("Dashboard server started at http://" + host + ":" + port);
        } catch (IOException exception) {
            this.server = null;
            this.freeframe.getLogger().warning("Could not start dashboard server: " + exception.getMessage());
        }
    }

    public synchronized void stop() {
        if (this.server != null) {
            this.server.stop(0);
            this.server = null;
        }
    }

    private String payloadFor(String type) {
        if ("health".equals(type)) {
            return "{\"status\":\"ok\",\"version\":\"" + escape(this.freeframe.getDescription().getVersion()) + "\"}";
        }
        if ("stats".equals(type)) {
            Map<String, Long> metrics = this.freeframe.getMetricsTracker().snapshot();
            StringBuilder builder = new StringBuilder();
            builder.append("{\"trackedFrames\":").append(this.freeframe.getFrameRegistry().size()).append(",\"metrics\":{");
            boolean first = true;
            for (Map.Entry<String, Long> entry : metrics.entrySet()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
                first = false;
            }
            builder.append("}}");
            return builder.toString();
        }

        List<FreeFrameData> frames = this.freeframe.getFrameRegistry().listFrames();
        StringBuilder builder = new StringBuilder();
        builder.append("{\"count\":").append(frames.size()).append(",\"frames\":[");
        for (int index = 0; index < frames.size(); index++) {
            FreeFrameData frame = frames.get(index);
            if (index > 0) {
                builder.append(",");
            }
            builder.append("{")
                .append("\"id\":\"").append(escape(frame.getId())).append("\",")
                .append("\"owner\":\"").append(escape(frame.getOwnerName())).append("\",")
                .append("\"shopOwnerType\":\"").append(escape(frame.getShopOwnerType().name())).append("\",")
                .append("\"saleMode\":\"").append(escape(frame.getSaleMode().name())).append("\",")
                .append("\"network\":\"").append(escape(frame.getNetworkId())).append("\",")
                .append("\"stock\":").append(frame.getStock()).append(",")
                .append("\"price\":").append(String.format(java.util.Locale.ENGLISH, "%.2f", frame.getPrice()))
                .append("}");
        }
        builder.append("]}");
        return builder.toString();
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String token = this.freeframe.getPluginConfig().getString("freeframe.dashboard.token", "");
        if (token == null || token.trim().isEmpty()) {
            return true;
        }
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return false;
        }
        String expected = "token=" + token;
        return query.contains(expected);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class JsonHandler implements HttpHandler {
        private final DashboardServer owner;
        private final String type;

        JsonHandler(DashboardServer owner, String type) {
            this.owner = owner;
            this.type = type;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            int status = 200;
            if (!this.owner.isAuthorized(exchange)) {
                response = "{\"error\":\"unauthorized\"}";
                status = 401;
            } else {
                response = this.owner.payloadFor(this.type);
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }
    }
}
