package com.zenmanage.sdk.api;

import com.sun.net.httpserver.HttpServer;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.context.Context;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiClientTest {
    private HttpServer server;
    private BlockingQueue<Map<String, List<String>>> capturedHeaders;
    private String endpoint;

    @BeforeEach
    void startServer() throws IOException {
        capturedHeaders = new ArrayBlockingQueue<>(10);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            capturedHeaders.offer(exchange.getRequestHeaders());
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        endpoint = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void reportUsageSendsDefaultValueHeader() throws InterruptedException {
        ApiClient client = new ApiClient("srv_test", endpoint, new TestLogger(), true, "0.1.0", "zenmanage-java");

        client.reportUsage("new-ui", null, true);

        Map<String, List<String>> headers = capturedHeaders.poll(5, TimeUnit.SECONDS);
        assertNotNull(headers);
        assertEquals("{\"new-ui\":true}", headers.get("X-Default-Value").get(0));
    }

    @Test
    void reportUsageSendsNonBooleanDefaultValueHeader() throws InterruptedException {
        ApiClient client = new ApiClient("srv_test", endpoint, new TestLogger(), true, "0.1.0", "zenmanage-java");

        client.reportUsage("num-flag", null, 42);

        Map<String, List<String>> headers = capturedHeaders.poll(5, TimeUnit.SECONDS);
        assertNotNull(headers);
        assertEquals("{\"num-flag\":42}", headers.get("X-Default-Value").get(0));
    }

    @Test
    void reportUsageOmitsDefaultValueHeaderWhenNotProvided() throws InterruptedException {
        ApiClient client = new ApiClient("srv_test", endpoint, new TestLogger(), true, "0.1.0", "zenmanage-java");

        client.reportUsage("new-ui", null);

        Map<String, List<String>> headers = capturedHeaders.poll(5, TimeUnit.SECONDS);
        assertNotNull(headers);
        assertFalse(headers.containsKey("X-Default-Value"));
    }

    @Test
    void reportUsageSendsBothContextAndDefaultValueHeaders() throws InterruptedException {
        ApiClient client = new ApiClient("srv_test", endpoint, new TestLogger(), true, "0.1.0", "zenmanage-java");

        client.reportUsage("num-flag", Context.single("user", "u-1"), 42);

        Map<String, List<String>> headers = capturedHeaders.poll(5, TimeUnit.SECONDS);
        assertNotNull(headers);
        assertEquals("{\"num-flag\":42}", headers.get("X-Default-Value").get(0));
        assertNotNull(headers.get("X-Zenmanage-Context"));
    }

    @Test
    void reportUsageDisabledSkipsRequest() throws InterruptedException {
        ApiClient client = new ApiClient("srv_test", endpoint, new TestLogger(), false, "0.1.0", "zenmanage-java");

        client.reportUsage("new-ui", null, true);

        assertEquals(null, capturedHeaders.poll(500, TimeUnit.MILLISECONDS));
    }

    private static final class TestLogger implements Logger {
        @Override
        public void debug(String message) {
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void warn(String message) {
        }

        @Override
        public void error(String message, Throwable throwable) {
        }
    }
}
