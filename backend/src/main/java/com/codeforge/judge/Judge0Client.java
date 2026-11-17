package com.codeforge.judge;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * HTTP boundary to a separately isolated Judge0 provider. This service NEVER
 * compiles or runs submitted code in the Spring Boot process.
 */
@Service
public class Judge0Client {
    private final RestClient client;
    private final boolean configured;
    private final int languageId;

    public record Verdict(int statusId, String description, String stdout, String stderr,
                          String compileOutput) {}

    public Judge0Client(@Value("${judge0.base-url:}") String baseUrl,
                        @Value("${judge0.language-id:62}") int languageId,
                        @Value("${judge0.api-key:}") String apiKey) {
        this.configured = baseUrl != null && !baseUrl.isBlank();
        this.languageId = languageId;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(8));
        var builder = RestClient.builder().requestFactory(factory);
        if (configured) {
            builder.baseUrl(baseUrl.strip().replaceAll("/+$", ""));
        }
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Auth-Token", apiKey);
        }
        this.client = builder.build();
    }

    public String submit(String sourceCode, String stdin, String expectedOutput) {
        requireConfigured();
        var request = Map.<String, Object>of(
            "language_id", languageId,
            "source_code", sourceCode,
            "stdin", stdin,
            "expected_output", expectedOutput,
            "cpu_time_limit", 2,
            "wall_time_limit", 5,
            "memory_limit", 524288,
            "max_file_size", 1024
        );
        try {
            JsonNode json = client.post()
                .uri("/submissions?base64_encoded=false&wait=false")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve().body(JsonNode.class);
            String token = field(json, "token");
            if (token == null || token.isBlank()) throw providerError();
            return token;
        } catch (RestClientException exception) {
            throw providerError();
        }
    }

    public Verdict result(String token) {
        requireConfigured();
        try {
            JsonNode json = client.get()
                .uri("/submissions/{token}?base64_encoded=false", token)
                .retrieve().body(JsonNode.class);
            if (json == null || !json.path("status").hasNonNull("id")) throw providerError();
            return new Verdict(json.path("status").path("id").asInt(),
                field(json.path("status"), "description"),
                field(json, "stdout"), field(json, "stderr"), field(json, "compile_output"));
        } catch (RestClientException exception) {
            throw providerError();
        }
    }

    private void requireConfigured() {
        if (!configured) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Judge0 is not configured. Set JUDGE0_BASE_URL to an isolated Judge0 instance.");
    }

    private static String field(JsonNode json, String key) {
        return json == null || json.path(key).isNull() || json.path(key).isMissingNode()
            ? null : json.path(key).asText();
    }

    private static ResponseStatusException providerError() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
            "Isolated execution provider is unavailable or returned an invalid response");
    }
}
