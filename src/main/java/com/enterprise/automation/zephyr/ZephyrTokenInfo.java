package com.enterprise.automation.zephyr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public record ZephyrTokenInfo(String issuer, String jiraBaseUrl, String accountId, Instant expiresAt) {
    public static final String ZEPHYR_ESSENTIAL_ISSUER = "com.thed.zephyr.je";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int JWT_PART_COUNT = 3;

    public static Optional<ZephyrTokenInfo> parse(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length != JWT_PART_COUNT) {
            return Optional.empty();
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode claims = MAPPER.readTree(new String(payload, StandardCharsets.UTF_8));
            JsonNode context = claims.path("context");
            long exp = claims.path("exp").asLong(0L);
            return Optional.of(new ZephyrTokenInfo(
                    claims.path("iss").asText(""),
                    context.path("baseUrl").asText(""),
                    context.path("user").path("accountId").asText(""),
                    exp > 0 ? Instant.ofEpochSecond(exp) : null));
        } catch (IllegalArgumentException | IOException unreadable) {
            return Optional.empty();
        }
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isZephyrEssential() {
        return ZEPHYR_ESSENTIAL_ISSUER.equals(issuer);
    }
}
