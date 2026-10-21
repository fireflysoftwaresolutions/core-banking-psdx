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

import io.fireflysoftware.core.banking.psdx.core.services.ConsentValidationService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ThirdPartyProviderService;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDConsentInvalidException;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Interceptor for validating consents in requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsentValidationInterceptor implements WebFilter {

    private final ConsentValidationService consentValidationService;
    private final ThirdPartyProviderService thirdPartyProviderService;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/auth/token",
            "/api/v1/auth/refresh",
            "/api/v1/providers/register",
            "/api/v1/providers/validate",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health",
            "/actuator/info"
    );

    /**
     * Filter method to validate consents.
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

        // Skip validation for paths that don't require consent
        if (!requiresConsent(path)) {
            return chain.filter(exchange);
        }

        // Get consent ID from header
        String consentIdHeader = exchange.getRequest().getHeaders().getFirst("X-Consent-ID");
        if (consentIdHeader == null || consentIdHeader.isEmpty()) {
            log.warn("Missing X-Consent-ID header for path: {}", path);
            return Mono.error(new PSDFormatException("Missing X-Consent-ID header", "Please provide a valid consent ID in the X-Consent-ID header"));
        }

        UUID consentId;
        try {
            consentId = UUID.fromString(consentIdHeader);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid X-Consent-ID header: {} for path: {}", consentIdHeader, path);
            return Mono.error(new PSDFormatException("Invalid X-Consent-ID header", "The consent ID must be a valid UUID"));
        }

        UUID partyId = partyId(exchange.getRequest());
        if (partyId == null) {
            log.warn("Missing party ID for path: {}", path);
            return Mono.error(new PSDFormatException("Missing party ID", "Please provide a valid party ID in the request parameters or PSU-ID header"));
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Missing third party ID for path: {}", path);
            return Mono.error(new PSDFormatException("Missing third party ID", "Please provide a valid API key in the X-API-KEY header"));
        }

        ResourceType resourceType = getResourceTypeFromPath(path);

        return thirdPartyProviderService.validateApiKey(apiKey)
                .switchIfEmpty(Mono.error(new PSDFormatException("Unknown API key", "The API key is not a registered provider")))
                .flatMap(provider -> consentValidationService.validateConsent(
                        consentId, resourceType, partyId, provider.getId().toString()))
                .flatMap(isValid -> {
                    if (isValid) {
                        return chain.filter(exchange);
                    } else {
                        log.warn("Invalid consent ID: {} for resource type: {}, party ID: {}",
                                consentId, resourceType, partyId);
                        return Mono.error(new PSDConsentInvalidException(
                                "Invalid consent",
                                "The provided consent is invalid, expired, or does not grant access to the requested resource"));
                    }
                });
    }

    /**
     * Check if a path is a public path that doesn't require consent validation.
     *
     * @param path The path
     * @return True if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Check if a path requires consent validation.
     *
     * @param path The path
     * @return True if the path requires consent validation, false otherwise
     */
    private boolean requiresConsent(String path) {
        return path.startsWith("/api/v1/accounts") ||
               path.startsWith("/api/v1/card-accounts") ||
               path.startsWith("/api/v1/payments") ||
               path.startsWith("/api/v1/funds-confirmations");
    }

    /**
     * Get the party ID from the request.
     *
     * @param request The request
     * @return The party ID, or null if not found
     */
    private UUID partyId(ServerHttpRequest request) {
        String partyIdParam = request.getQueryParams().getFirst("partyId");
        if (partyIdParam != null && !partyIdParam.isEmpty()) {
            return parseParty(partyIdParam);
        }
        String psuId = request.getHeaders().getFirst("PSU-ID");
        if (psuId != null && !psuId.isEmpty()) {
            return parseParty(psuId);
        }
        return null;
    }

    private UUID parseParty(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            log.warn("Party id is not a UUID: {}", value);
            return null;
        }
    }

    /**
     * Get the resource type from the path.
     *
     * @param path The path
     * @return The resource type
     */
    private ResourceType getResourceTypeFromPath(String path) {
        if (path.contains("/accounts") && !path.contains("/card-accounts")) {
            if (path.contains("/balances")) {
                return ResourceType.BALANCE;
            } else if (path.contains("/transactions")) {
                return ResourceType.TRANSACTION;
            } else {
                return ResourceType.ACCOUNT;
            }
        } else if (path.contains("/card-accounts")) {
            if (path.contains("/balances")) {
                return ResourceType.CARD_BALANCE;
            } else if (path.contains("/transactions")) {
                return ResourceType.CARD_TRANSACTION;
            } else {
                return ResourceType.CARD;
            }
        } else if (path.contains("/payments")) {
            return ResourceType.PAYMENT;
        } else if (path.contains("/funds-confirmations")) {
            return ResourceType.FUNDS_CONFIRMATION;
        } else {
            return ResourceType.CONSENT;
        }
    }
}
