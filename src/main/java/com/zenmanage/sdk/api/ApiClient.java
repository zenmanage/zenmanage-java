package com.zenmanage.sdk.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.errors.FetchRulesException;
import com.zenmanage.sdk.errors.InvalidRulesException;
import com.zenmanage.sdk.flags.RulesResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP API client for fetching rules and reporting usage.
 */
public class ApiClient {
    private static final String RULES_PATH = "/v1/flag-json";
    private static final int MAX_RETRIES = 3;
    private static final int BASE_RETRY_DELAY_MS = 100;

    private final String apiEndpoint;
    private final Logger logger;
    private final boolean usageReportingEnabled;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> baseHeaders;

    public ApiClient(
        String environmentToken,
        String apiEndpoint,
        Logger logger,
        boolean usageReportingEnabled,
        String sdkVersion,
        String clientAgent
    ) {
        this(environmentToken, apiEndpoint, logger, usageReportingEnabled, sdkVersion, clientAgent,
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    ApiClient(
        String environmentToken,
        String apiEndpoint,
        Logger logger,
        boolean usageReportingEnabled,
        String sdkVersion,
        String clientAgent,
        HttpClient httpClient,
        ObjectMapper objectMapper
    ) {
        this.apiEndpoint = apiEndpoint;
        this.logger = logger;
        this.usageReportingEnabled = usageReportingEnabled;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseHeaders = new HashMap<>();
        baseHeaders.put("Accept", "application/json");
        baseHeaders.put("Content-Type", "application/json");
        baseHeaders.put("X-API-Key", environmentToken);
        baseHeaders.put("X-ZEN-CLIENT-AGENT", clientAgent + "/" + sdkVersion);
    }

    public RulesResponse getRules() {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    long delayMs = (long) (BASE_RETRY_DELAY_MS * Math.pow(2, attempt - 1));
                    Thread.sleep(delayMs);
                }

                String cdnUrl = getCdnRulesUrl();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cdnUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new FetchRulesException("CDN request failed with status " + response.statusCode(), response.statusCode());
                }

                RulesResponse rules = objectMapper.readValue(response.body(), RulesResponse.class);
                if (rules == null || rules.getVersion() == null || rules.getFlags() == null) {
                    throw new InvalidRulesException("Invalid rules response format from CDN");
                }

                return rules;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new FetchRulesException("Interrupted while fetching rules", exception);
            } catch (IOException | FetchRulesException | InvalidRulesException exception) {
                lastException = exception;
                logger.warn("Failed to fetch rules attempt " + (attempt + 1) + "/" + MAX_RETRIES);
            }
        }

        throw new FetchRulesException("Failed to fetch rules after retries", lastException);
    }

    public void reportUsage(String key, Context context) {
        reportUsage(key, context, null);
    }

    public void reportUsage(String key, Context context, Object defaultValue) {
        if (!usageReportingEnabled) {
            return;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(apiEndpoint + "/v1/flags/" + key + "/usage"))
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.noBody());

        for (Map.Entry<String, String> entry : baseHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }

        if (context != null && shouldSendContext(context)) {
            try {
                requestBuilder.header("X-ZENMANAGE-CONTEXT", objectMapper.writeValueAsString(context.toMap()));
            } catch (JsonProcessingException exception) {
                logger.debug("Failed to encode usage context");
            }
        }

        if (defaultValue != null) {
            try {
                requestBuilder.header("X-DEFAULT-VALUE", objectMapper.writeValueAsString(Map.of(key, defaultValue)));
            } catch (JsonProcessingException exception) {
                logger.debug("Failed to encode usage default value");
            }
        }

        httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.discarding())
            .exceptionally(exception -> {
                logger.debug("Failed to report usage: " + exception.getMessage());
                return null;
            });
    }

    private String getCdnRulesUrl() throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(apiEndpoint + RULES_PATH))
            .timeout(Duration.ofSeconds(10))
            .GET();

        for (Map.Entry<String, String> entry : baseHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new FetchRulesException("API metadata request failed with status " + response.statusCode(), response.statusCode());
        }

        MetadataResponse metadata = objectMapper.readValue(response.body(), MetadataResponse.class);
        if (metadata == null || metadata.data == null || metadata.data.cdn == null || metadata.data.path == null) {
            throw new InvalidRulesException("API response missing cdn or path fields");
        }

        return metadata.data.cdn + metadata.data.path;
    }

    private boolean shouldSendContext(Context context) {
        return !(
            "anonymous".equals(context.getType())
                && context.getName() == null
                && context.getIdentifier() == null
                && context.getAttributes().isEmpty()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class MetadataResponse {
        public Data data;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class Data {
            public String cdn;
            public String path;
        }
    }
}
