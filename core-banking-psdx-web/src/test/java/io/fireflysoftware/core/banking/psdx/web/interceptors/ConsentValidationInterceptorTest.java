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
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDConsentInvalidException;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for the ConsentValidationInterceptor.
 */
@ExtendWith(MockitoExtension.class)
class ConsentValidationInterceptorTest {

    @Mock
    private ConsentValidationService consentValidationService;

    @Mock
    private WebFilterChain chain;

    private ConsentValidationInterceptor interceptor;

    // Test constants
    private static final String VALID_CONSENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_PARTY_ID = "550e8400-e29b-41d4-a716-446655440001";

    @BeforeEach
    void setUp() {
        interceptor = new ConsentValidationInterceptor(consentValidationService);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldPassRequest_whenPathIsPublic() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldPassRequest_whenPathDoesNotRequireConsent() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/providers/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldReturnBadRequest_whenConsentIdHeaderIsMissing() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDFormatException &&
                        throwable.getMessage().contains("Missing X-Consent-ID header"))
                .verify();
    }

    @Test
    void filter_shouldReturnBadRequest_whenConsentIdHeaderIsInvalid() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Consent-ID", "invalid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDFormatException &&
                        throwable.getMessage().contains("Invalid X-Consent-ID header"))
                .verify();
    }

    @Test
    void filter_shouldReturnBadRequest_whenPartyIdIsMissing() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Consent-ID", VALID_CONSENT_ID)
                .header("X-API-KEY", "api-key-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDFormatException &&
                        throwable.getMessage().contains("Missing party ID"))
                .verify();
    }

    @Test
    void filter_shouldReturnBadRequest_whenThirdPartyIdIsMissing() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Consent-ID", VALID_CONSENT_ID)
                .queryParam("partyId", VALID_PARTY_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDFormatException &&
                        throwable.getMessage().contains("Missing third party ID"))
                .verify();
    }

    @Test
    void filter_shouldPassRequest_whenConsentIsValid() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Consent-ID", VALID_CONSENT_ID)
                .header("X-API-KEY", "api-key-123")
                .queryParam("partyId", VALID_PARTY_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(consentValidationService.validateConsent(any(UUID.class), any(ResourceType.class), any(UUID.class), eq("api-key-123")))
                .thenReturn(Mono.just(true));

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldReturnForbidden_whenConsentIsInvalid() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Consent-ID", VALID_CONSENT_ID)
                .header("X-API-KEY", "api-key-123")
                .queryParam("partyId", VALID_PARTY_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(consentValidationService.validateConsent(any(UUID.class), any(ResourceType.class), any(UUID.class), eq("api-key-123")))
                .thenReturn(Mono.just(false));

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDConsentInvalidException &&
                        throwable.getMessage().contains("Invalid consent"))
                .verify();
    }
}
