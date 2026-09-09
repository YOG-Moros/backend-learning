package com.example.config.model;

import java.util.List;

public record ConfigResponse(
        String module,
        String name,
        String environment,
        boolean featuresEnabled,
        long requestTimeoutMilliseconds,
        List<String> allowedOrigins,
        List<String> activeProfiles
) {
}
