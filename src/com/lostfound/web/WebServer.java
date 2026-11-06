package com.lostfound.web;

import com.lostfound.model.Claim;
import com.lostfound.model.Item;
import com.lostfound.service.ClaimService;
import com.lostfound.service.ItemService;
import com.lostfound.service.UserService;
import com.lostfound.util.Theme;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {

    private static HttpServer server;
    private static int activePort = 8080;
    private static boolean running = false;
    private static final ItemService itemService = new ItemService();
    private static final ClaimService claimService = new ClaimService();
    private static final UserService userService = new UserService();

    public static synchronized void start() {
        if (running) return;

        int[] portsToTry = {8080, 8081, 8082, 8090, 8888};
        for (int p : portsToTry) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                activePort = p;
                break;
            } catch (IOException e) {
                // try next port
            }
        }

        if (server == null) {
            System.err.println("Could not bind WebServer to any available port.");
            return;
        }

        // Static Web Files
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/status", new ApiStatusHandler());
        server.createContext("/api/stats", new ApiStatsHandler());
        server.createContext("/api/items", new ApiItemsHandler());
        server.createContext("/api/claims", new ApiClaimsHandler());
        server.createContext("/api/theme", new ApiThemeHandler());

        // Image Files Handler
        server.createContext("/data/", new DataImagesHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        running = true;
        System.out.println("Lost & Found Web Server running at: http://localhost:" + activePort);
    }

    public static synchronized void stop() {
        if (server != null && running) {
            server.stop(0);
            running = false;
        }
    }

    public static int getActivePort() {
        return activePort;
    }

    public static String getUrl() {
        return "http://localhost:" + activePort;
    }

    public static void openInBrowser() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(getUrl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // DIRECTORY LOCATORS
    // =====================================================

    private static File findWebsiteDirectory() {
        String[] candidates = {
                "d:/Program Files/Java/LostFound/src/com/lostfound/lostfoundwebsite",
                "d:/Program Files/Java/LostFound/out/production/LostFound/com/lostfound/lostfoundwebsite",
                "src/com/lostfound/lostfoundwebsite",
                "out/production/LostFound/com/lostfound/lostfoundwebsite"
        };
        for (String c : candidates) {
            File dir = new File(c);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File("d:/Program Files/Java/LostFound/src/com/lostfound/lostfoundwebsite");
    }

    private static File findDataDirectory() {
        String[] candidates = {
                "d:/Program Files/Java/LostFound/data",
                "data"
        };
        for (String c : candidates) {
            File dir = new File(c);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        File f = new File("d:/Program Files/Java/LostFound/data");
        if (!f.exists()) f.mkdirs();
        return f;
    }

    // =====================================================
    // STATIC FILE SERVING
    // =====================================================

    private static class StaticFileHandler implements HttpHandler {
        private final File websiteDir = findWebsiteDirectory();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File requestedFile = new File(websiteDir, path.startsWith("/") ? path.substring(1) : path);
            if (!requestedFile.exists() || requestedFile.isDirectory()) {
                sendResponse(exchange, 404, "text/plain; charset=UTF-8", "404 Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String mime = getMimeType(requestedFile.getName());
            byte[] bytes = java.nio.file.Files.readAllBytes(requestedFile.toPath());
            sendResponse(exchange, 200, mime, bytes);
        }
    }

    // =====================================================
    // DATA IMAGES SERVING (FOR ATTACHED PROOFS AND ITEM PHOTOS)
    // =====================================================

    private static class DataImagesHandler implements HttpHandler {
        private final File dataDir = findDataDirectory();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String subPath = path;
            if (subPath.startsWith("/data/")) {
                subPath = subPath.substring(6);
            } else if (subPath.startsWith("/")) {
                subPath = subPath.substring(1);
            }
            File file = new File(dataDir, subPath);
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "text/plain", "Image Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String mime = getMimeType(file.getName());
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            sendResponse(exchange, 200, mime, bytes);
        }
    }

    // =====================================================
    // REST API HANDLERS
    // =====================================================

    private static class ApiStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String json = "{"
                    + "\"status\":\"ONLINE\","
                    + "\"platform\":\"Lost & Found\","
                    + "\"theme\":\"" + (Theme.isNvidia() ? "nvidia" : "netflix") + "\","
                    + "\"themeDisplay\":\"" + Theme.getCurrentTheme().getDisplayName() + "\","
                    + "\"totalItems\":" + itemService.getAllItems().size() + ","
                    + "\"totalClaims\":" + claimService.getAllClaims().size()
                    + "}";

            sendResponse(exchange, 200, "application/json; charset=UTF-8", json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class ApiStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            List<Item> items = itemService.getAllItems();
            long lostCount = items.stream().filter(i -> "LOST".equalsIgnoreCase(i.getStatus()) || "LOST".equalsIgnoreCase(i.getItemType())).count();
            long foundCount = items.stream().filter(i -> "FOUND".equalsIgnoreCase(i.getStatus()) || "FOUND".equalsIgnoreCase(i.getItemType())).count();
            List<Claim> claims = claimService.getAllClaims();
            long pendingClaims = claims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count();
            long approvedClaims = claims.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getClaimStatus()) || "RESOLVED".equalsIgnoreCase(c.getClaimStatus())).count();

            String json = "{"
                    + "\"totalItems\":" + items.size() + ","
                    + "\"lostCount\":" + lostCount + ","
                    + "\"foundCount\":" + foundCount + ","
                    + "\"usersCount\":" + userService.getAllUsers().size() + ","
                    + "\"pendingClaims\":" + pendingClaims + ","
                    + "\"approvedClaims\":" + approvedClaims
                    + "}";

            sendResponse(exchange, 200, "application/json; charset=UTF-8", json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class ApiItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Item> items = itemService.getAllItems();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    sb.append("{")
                            .append("\"id\":").append(it.getId()).append(",")
                            .append("\"itemName\":").append(escapeJson(it.getItemName())).append(",")
                            .append("\"category\":").append(escapeJson(it.getCategory())).append(",")
                            .append("\"itemType\":").append(escapeJson(it.getItemType())).append(",")
                            .append("\"location\":").append(escapeJson(it.getLocation())).append(",")
                            .append("\"description\":").append(escapeJson(it.getDescription())).append(",")
                            .append("\"status\":").append(escapeJson(it.getStatus())).append(",")
                            .append("\"ownerName\":").append(escapeJson(it.getOwnerName())).append(",")
                            .append("\"contact\":").append(escapeJson(it.getContact())).append(",")
                            .append("\"imagePath\":").append(escapeJson(it.getImagePath() != null ? it.getImagePath() : ""))
                            .append("}");
                    if (i < items.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                Map<String, String> params = parseFormOrJson(body);

                Item item = new Item();
                item.setItemName(params.getOrDefault("itemName", "Reported Item"));
                item.setCategory(params.getOrDefault("category", "General"));

                String type = params.getOrDefault("itemType", "LOST").toUpperCase();
                item.setItemType(type);
                item.setStatus(type);

                item.setLocation(params.getOrDefault("location", "Not specified"));
                item.setDescription(params.getOrDefault("description", ""));
                item.setOwnerName(params.getOrDefault("ownerName", "Community Member"));
                item.setContact(params.getOrDefault("contact", ""));
                item.setReportedByUsername("web_user");
                item.setItemDate(new Date());
                item.setQuantity(1);
                item.setUrgency(2);

                // Handle optional photo uploaded from website
                String base64Image = params.get("imageBase64");
                if (base64Image != null && !base64Image.trim().isEmpty()) {
                    try {
                        String clean = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
                        byte[] decoded = Base64.getDecoder().decode(clean);
                        File imgFolder = new File(findDataDirectory(), "item_images");
                        if (!imgFolder.exists()) imgFolder.mkdirs();
                        File targetFile = new File(imgFolder, "web_item_" + System.currentTimeMillis() + ".png");
                        java.nio.file.Files.write(targetFile.toPath(), decoded);
                        item.setImagePath(targetFile.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (params.containsKey("imagePath") && !params.get("imagePath").isEmpty()) {
                    item.setImagePath(params.get("imagePath"));
                } else {
                    item.setImagePath("");
                }

                itemService.addItem(item);
                boolean success = true;
                String resp = "{\"success\":" + success + ",\"id\":" + item.getId() + ",\"type\":\"" + type + "\"}";
                sendResponse(exchange, success ? 200 : 400, "application/json; charset=UTF-8", resp.getBytes(StandardCharsets.UTF_8));
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static class ApiClaimsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Claim> claims = claimService.getAllClaims();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < claims.size(); i++) {
                    Claim c = claims.get(i);
                    sb.append("{")
                            .append("\"id\":").append(c.getId()).append(",")
                            .append("\"itemId\":").append(c.getItemId()).append(",")
                            .append("\"claimantName\":").append(escapeJson(c.getClaimantName())).append(",")
                            .append("\"claimantUsername\":").append(escapeJson(c.getClaimantUsername())).append(",")
                            .append("\"contact\":").append(escapeJson(c.getContact())).append(",")
                            .append("\"proof\":").append(escapeJson(c.getProof())).append(",")
                            .append("\"claimStatus\":").append(escapeJson(c.getClaimStatus())).append(",")
                            .append("\"imagePath\":").append(escapeJson(c.getImagePath() != null ? c.getImagePath() : ""))
                            .append("}");
                    if (i < claims.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                Map<String, String> params = parseFormOrJson(body);

                int itemId = 0;
                try {
                    itemId = Integer.parseInt(params.getOrDefault("itemId", "0"));
                } catch (Exception ignored) {}

                String claimant = params.getOrDefault("claimantName", "Web Claimant");
                String contact = params.getOrDefault("contact", "");
                String proof = params.getOrDefault("proof", "");

                Claim claim = new Claim();
                claim.setItemId(itemId);
                claim.setClaimantName(claimant);
                claim.setClaimantUsername("web_user");
                claim.setContact(contact);
                claim.setProof(proof);
                claim.setClaimStatus("PENDING");
                claim.setClaimDate(new Date());

                // Handle optional proof photo uploaded from website
                String base64Proof = params.get("proofBase64");
                if (base64Proof != null && !base64Proof.trim().isEmpty()) {
                    try {
                        String clean = base64Proof.replaceFirst("^data:image/[^;]+;base64,", "");
                        byte[] decoded = Base64.getDecoder().decode(clean);
                        File proofFolder = new File(findDataDirectory(), "claim_proofs");
                        if (!proofFolder.exists()) proofFolder.mkdirs();
                        File targetFile = new File(proofFolder, "web_proof_" + System.currentTimeMillis() + ".png");
                        java.nio.file.Files.write(targetFile.toPath(), decoded);
                        claim.setImagePath(targetFile.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (params.containsKey("imagePath") && !params.get("imagePath").isEmpty()) {
                    claim.setImagePath(params.get("imagePath"));
                } else {
                    claim.setImagePath("");
                }

                claimService.addClaim(claim);
                boolean success = true;
                String resp = "{\"success\":" + success + ",\"id\":" + claim.getId() + "}";
                sendResponse(exchange, success ? 200 : 400, "application/json; charset=UTF-8", resp.getBytes(StandardCharsets.UTF_8));
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static class ApiThemeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange).toLowerCase();
                if (body.contains("netflix")) {
                    Theme.setTheme(Theme.ThemeMode.NETFLIX);
                } else if (body.contains("nvidia")) {
                    Theme.setTheme(Theme.ThemeMode.NVIDIA);
                } else {
                    Theme.toggleTheme();
                }
            }

            String current = Theme.isNvidia() ? "nvidia" : "netflix";
            String resp = "{\"theme\":\"" + current + "\",\"display\":\"" + Theme.getCurrentTheme().getDisplayName() + "\"}";
            sendResponse(exchange, 200, "application/json; charset=UTF-8", resp.getBytes(StandardCharsets.UTF_8));
        }
    }

    // =====================================================
    // HELPER UTILITIES
    // =====================================================

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseFormOrJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        body = body.trim();
        if (body.startsWith("{")) {
            // Robust regex parser for JSON key-value pairs
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\]\\s]+))");
            Matcher matcher = pattern.matcher(body);
            while (matcher.find()) {
                String key = matcher.group(1);
                String val = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                map.put(key, val);
            }
        } else {
            // URL Encoded form
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    try {
                        String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                        String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                        map.put(k, v);
                    } catch (Exception ignored) {}
                }
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static String getMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        return "text/plain; charset=UTF-8";
    }
}
