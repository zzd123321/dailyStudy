package com.dailystudy.day002;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** A loopback-only HTTP teaching tool. State is in memory, without authentication. */
public final class HttpPlayground {
    private static final int MAX_BODY_BYTES = 8192;
    private final ObjectMapper json = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private final Map<Integer, Note> notes = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(2);

    public record Note(int id, String title) {}

    private HttpPlayground() {
        notes.put(1, new Note(1, "第1天：Java工具链"));
    }

    public static HttpServer start(int port) throws IOException {
        HttpPlayground app = new HttpPlayground();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", app::handle);
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8082 : Integer.parseInt(args[0]);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口须为 1–65535");
        }
        HttpServer server = start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        System.out.println("HTTP 实验服务已启动，端口 " + server.getAddress().getPort());
        System.out.println("在本地浏览器地址栏输入：http://127.0.0.1:" + port);
        System.out.println("数据只保存在内存，重启会恢复初始数据。按 Ctrl+C 停止。");
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            route(exchange);
        } catch (JsonProcessingException e) {
            error(exchange, 400, "invalid_json", "请求正文不是有效的单个 JSON 值");
        } catch (IllegalArgumentException e) {
            error(exchange, 400, "invalid_query", "limit 须为 1–20 的整数，且只能指定一次");
        } catch (Exception e) {
            // No submitted content or credentials are included in the response.
            System.err.println("实验服务异常：" + e.getClass().getSimpleName());
            error(exchange, 500, "internal_error", "服务端处理失败");
        } finally {
            exchange.close();
        }
    }

    private void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/")) {
            if (!method.equals("GET")) {
                methodNotAllowed(exchange, "GET");
                return;
            }
            try (var stream = HttpPlayground.class.getResourceAsStream("/index.html")) {
                if (stream == null) throw new IllegalStateException("missing index.html");
                send(exchange, 200, "text/html; charset=utf-8", stream.readAllBytes());
            }
            return;
        }

        if (path.equals("/api/notes")) {
            if (method.equals("GET")) {
                int limit = parseLimit(exchange.getRequestURI().getRawQuery());
                var items = notes.values().stream()
                        .sorted(Comparator.comparingInt(Note::id)).limit(limit).toList();
                sendJson(exchange, 200, Map.of("items", items, "total", notes.size()));
            } else if (method.equals("POST")) {
                createNote(exchange);
            } else {
                methodNotAllowed(exchange, "GET, POST");
            }
            return;
        }

        if (path.matches("/api/notes/[0-9]+")) {
            if (!method.equals("GET")) {
                methodNotAllowed(exchange, "GET");
                return;
            }
            Note note = null;
            try {
                note = notes.get(Integer.parseInt(path.substring("/api/notes/".length())));
            } catch (NumberFormatException ignored) {
                // A numeric identifier beyond int range cannot identify a note here.
            }
            if (note == null) {
                error(exchange, 404, "note_not_found", "知识条目不存在");
            } else {
                sendJson(exchange, 200, note);
            }
            return;
        }

        if (path.equals("/api/demo/failure")) {
            if (!method.equals("GET")) {
                methodNotAllowed(exchange, "GET");
                return;
            }
            error(exchange, 500, "demo_failure", "这是课程用的模拟服务端失败");
            return;
        }

        error(exchange, 404, "route_not_found", "接口不存在");
    }

    private void createNote(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";", 2)[0].strip()
                .equalsIgnoreCase("application/json")) {
            error(exchange, 415, "unsupported_media_type", "请使用 Content-Type: application/json");
            return;
        }
        byte[] body = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            error(exchange, 413, "body_too_large", "请求正文最多 8192 字节");
            return;
        }
        JsonNode input = json.readTree(body);
        if (input == null || input.isMissingNode()) {
            error(exchange, 400, "invalid_json", "请求正文缺少 JSON 值");
            return;
        }
        if (!input.isObject() || !input.path("title").isTextual()) {
            error(exchange, 422, "invalid_title", "title 须为非空字符串，最长 120 个字符");
            return;
        }
        String title = input.path("title").asText().strip();
        if (title.isEmpty() || title.codePointCount(0, title.length()) > 120) {
            error(exchange, 422, "invalid_title", "title 须为非空字符串，最长 120 个字符");
            return;
        }
        Note note = new Note(nextId.getAndIncrement(), title);
        notes.put(note.id(), note);
        exchange.getResponseHeaders().set("Location", "/api/notes/" + note.id());
        sendJson(exchange, 201, note);
    }

    private static int parseLimit(String query) {
        int limit = 10;
        boolean found = false;
        if (query != null) {
            for (String part : query.split("&")) {
                String[] pair = part.split("=", 2);
                if (URLDecoder.decode(pair[0], StandardCharsets.UTF_8).equals("limit")) {
                    if (found || pair.length != 2) throw new IllegalArgumentException();
                    found = true;
                    limit = Integer.parseInt(URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                }
            }
        }
        if (limit < 1 || limit > 20) throw new IllegalArgumentException();
        return limit;
    }

    private void methodNotAllowed(HttpExchange exchange, String allow) throws IOException {
        exchange.getResponseHeaders().set("Allow", allow);
        error(exchange, 405, "method_not_allowed", "此接口不支持该请求方法");
    }

    private void error(HttpExchange exchange, int status, String code, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", Map.of("code", code, "message", message)));
    }

    private void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", json.writeValueAsBytes(payload));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
        }
    }
}
