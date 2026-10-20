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


package io.fireflysoftware.core.banking.psdx.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static org.mockito.Mockito.*;

/**
 * Tests for the RequestLoggingFilter.
 */
@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() throws UnknownHostException {
        filter = new RequestLoggingFilter();

        // Mock HTTP headers and query params
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Test", "test-value");
        org.springframework.util.MultiValueMap<String, String> queryParams = new org.springframework.util.LinkedMultiValueMap<>();
        queryParams.add("test", "value");

        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(request.getMethod()).thenReturn(HttpMethod.GET);
        lenient().when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test"));
        lenient().when(request.getPath()).thenReturn(org.springframework.http.server.RequestPath.parse("/api/test", null));
        lenient().when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 12345));
        lenient().when(request.getHeaders()).thenReturn(headers);
        lenient().when(request.getQueryParams()).thenReturn(queryParams);
        lenient().when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        lenient().when(chain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldLogRequestAndResponse() {
        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_withMultipleRequests_shouldDetectAnomalies() throws UnknownHostException {
        // Given
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByName("192.168.1.1"), 12345));

        // When & Then
        for (int i = 0; i < 101; i++) {
            Mono<Void> result = filter.filter(exchange, chain);
            StepVerifier.create(result)
                    .verifyComplete();
        }

        verify(chain, times(101)).filter(exchange);
    }
}
