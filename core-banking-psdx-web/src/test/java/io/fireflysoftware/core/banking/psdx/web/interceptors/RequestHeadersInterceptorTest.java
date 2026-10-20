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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Tests for the RequestHeadersInterceptor.
 */
@ExtendWith(MockitoExtension.class)
class RequestHeadersInterceptorTest {

    @Mock
    private WebFilterChain chain;

    private RequestHeadersInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RequestHeadersInterceptor();
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldPassRequest_whenPathIsPublic() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldPassRequest_whenPathIsSwaggerUI() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/webjars/swagger-ui/index.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldPassRequest_whenPathIsSwaggerUIResource() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldReturnBadRequest_whenRequestIdHeaderIsMissing() {
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
                        throwable.getMessage().contains("Missing X-Request-ID header"))
                .verify();
    }

    @Test
    void filter_shouldReturnBadRequest_whenConsentIdHeaderIsMissingForAccountPath() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Request-ID", "request-id-123")
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
    void filter_shouldReturnBadRequest_whenPsuIdHeaderIsMissingForAccountPath() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Request-ID", "request-id-123")
                .header("X-Consent-ID", "consent-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PSDFormatException &&
                        throwable.getMessage().contains("Missing PSU-ID header"))
                .verify();
    }

    @Test
    void filter_shouldPassRequest_whenAllRequiredHeadersArePresent() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/accounts")
                .header("X-Request-ID", "request-id-123")
                .header("X-Consent-ID", "consent-id-123")
                .header("PSU-ID", "psu-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldPassRequest_whenPathDoesNotRequireConsentHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/providers")
                .header("X-Request-ID", "request-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldPassRequest_whenPathDoesNotRequirePsuHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/consents")
                .header("X-Request-ID", "request-id-123")
                .header("X-Consent-ID", "consent-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = interceptor.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
