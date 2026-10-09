package com.dailystudy.day002;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpPlaygroundTest {
    private HttpServer server;
    private String base;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();

    @BeforeEach
    void start() throws Exception {
        server = HttpPlayground.start(0);
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
        client.close();
    }

    private HttpResponse<String> request(String method, String path, String contentType, String body)
            throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(3));
        if (contentType != null) builder.header("Content-Type", contentType);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode body(HttpResponse<String> response) throws Exception {
        return json.readTree(response.body());
    }

    @Test
    void servesBrowserLabAndInitialChineseNote() throws Exception {
        var page = request("GET", "/", null, null);
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("request-form"));
        var list = request("GET", "/api/notes?limit=1", null, null);
        assertEquals(200, list.statusCode());
        assertTrue(list.headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
        assertEquals("第1天：Java工具链", body(list).path("items").get(0).path("title").asText());
    }

    @Test
    void createReturnsLocationAndCanBeReadBack() throws Exception {
        var created = request("POST", "/api/notes", "application/json; charset=utf-8", "{\"title\":\"  理解 HTTP 请求  \"}");
        assertEquals(201, created.statusCode());
        String location = created.headers().firstValue("Location").orElseThrow();
        var fetched = request("GET", location, null, null);
        assertEquals(200, fetched.statusCode());
        assertEquals(body(created), body(fetched));
        assertEquals("理解 HTTP 请求", body(fetched).path("title").asText());
        assertEquals(2, body(request("GET", "/api/notes", null, null)).path("total").asInt());
    }

    @Test
    void rejectsMalformedAndTrailingJsonWithoutCreatingNotes() throws Exception {
        for (String input : new String[]{"", "   ", "{\"title\":", "{\"title\":\"x\"} {}"}) {
            assertEquals(400, request("POST", "/api/notes", "application/json", input).statusCode());
        }
        assertEquals(1, body(request("GET", "/api/notes", null, null)).path("total").asInt());
    }

    @Test
    void rejectsSemanticallyInvalidTitles() throws Exception {
        for (String input : new String[]{"{}", "[]", "{\"title\":123}", "{\"title\":\"  \"}",
                json.writeValueAsString(java.util.Map.of("title", "字".repeat(121)))}) {
            assertEquals(422, request("POST", "/api/notes", "application/json", input).statusCode());
        }
        assertEquals(1, body(request("GET", "/api/notes", null, null)).path("total").asInt());
    }

    @Test
    void rejectsWrongOrMissingMediaTypeAndOversizedBody() throws Exception {
        assertEquals(415, request("POST", "/api/notes", null, "{}").statusCode());
        assertEquals(415, request("POST", "/api/notes", "text/plain", "{}").statusCode());
        assertEquals(413, request("POST", "/api/notes", "application/json", "x".repeat(8193)).statusCode());
    }

    @Test
    void validatesLimitAndDoesNotTreatUnknownNoteAsSuccess() throws Exception {
        for (String query : new String[]{"0", "21", "abc", "1&limit=2", ""}) {
            assertEquals(400, request("GET", "/api/notes?limit=" + query, null, null).statusCode());
        }
        assertEquals(404, request("GET", "/api/notes/999999", null, null).statusCode());
        assertEquals(404, request("GET", "/api/unknown", null, null).statusCode());
    }

    @Test
    void wrongMethodAdvertisesAllowedMethods() throws Exception {
        var response = request("DELETE", "/api/notes", null, null);
        assertEquals(405, response.statusCode());
        assertEquals("GET, POST", response.headers().firstValue("Allow").orElseThrow());
        assertEquals("method_not_allowed", body(response).path("error").path("code").asText());
    }

    @Test
    void failureDemoDoesNotStopTheService() throws Exception {
        var failed = request("GET", "/api/demo/failure", null, null);
        assertEquals(500, failed.statusCode());
        assertEquals("demo_failure", body(failed).path("error").path("code").asText());
        assertEquals(200, request("GET", "/api/notes", null, null).statusCode());
    }

    @Test
    void aFreshServerResetsInMemoryData() throws Exception {
        assertEquals(201, request("POST", "/api/notes", "application/json", "{\"title\":\"临时数据\"}").statusCode());
        server.stop(0);
        server = HttpPlayground.start(0);
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        assertEquals(1, body(request("GET", "/api/notes", null, null)).path("total").asInt());
    }
}
