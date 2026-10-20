/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.core.banking.psdx.web.interceptors;

import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Interceptor for validating required headers in requests.
 */
@Component
@Slf4j
public class RequestHeadersInterceptor implements WebFilter {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/auth/token",
            "/api/v1/auth/refresh",
            "/api/v1/providers/register",
            "/api/v1/providers/validate",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/webjars/swagger-ui",
            "/actuator/health",
            "/actuator/info"
    );

    /**
     * Filter method to validate required headers.
     *
     * @param exchange The server web exchange
     * @param chain The web filter chain
     * @return A Mono of Void
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip validation for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Validate X-Request-ID header
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            log.warn("Missing X-Request-ID header for path: {}", path);
            return Mono.error(new PSDFormatException(
                    "Missing X-Request-ID header",
                    "Please provide a unique request ID in the X-Request-ID header"));
        }

        // Validate X-Consent-ID header for paths that require it
        if (requiresConsent(path)) {
            String consentId = exchange.getRequest().getHeaders().getFirst("X-Consent-ID");
            if (consentId == null || consentId.isEmpty()) {
                log.warn("Missing X-Consent-ID header for path: {}", path);
                return Mono.error(new PSDFormatException(
                        "Missing X-Consent-ID header",
                        "Please provide a valid consent ID in the X-Consent-ID header"));
            }
        }

        // Validate PSU headers for account and payment operations
        if (isAccountOrPaymentPath(path)) {
            String psuId = exchange.getRequest().getHeaders().getFirst("PSU-ID");
            if (psuId == null || psuId.isEmpty()) {
                log.warn("Missing PSU-ID header for path: {}", path);
                return Mono.error(new PSDFormatException(
                        "Missing PSU-ID header",
                        "Please provide a valid PSU ID in the PSU-ID header"));
            }
        }

        return chain.filter(exchange);
    }

    /**
     * Check if a path is a public path that doesn't require header validation.
     *
     * @param path The path
     * @return True if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Check if a path requires a consent header.
     *
     * @param path The path
     * @return True if the path requires a consent header, false otherwise
     */
    private boolean requiresConsent(String path) {
        return path.startsWith("/api/v1/accounts") ||
               path.startsWith("/api/v1/card-accounts") ||
               path.startsWith("/api/v1/payments") ||
               path.startsWith("/api/v1/funds-confirmations");
    }

    /**
     * Check if a path is an account or payment path that requires PSU headers.
     *
     * @param path The path
     * @return True if the path is an account or payment path, false otherwise
     */
    private boolean isAccountOrPaymentPath(String path) {
        return path.startsWith("/api/v1/accounts") ||
               path.startsWith("/api/v1/card-accounts") ||
               path.startsWith("/api/v1/payments");
    }
}
