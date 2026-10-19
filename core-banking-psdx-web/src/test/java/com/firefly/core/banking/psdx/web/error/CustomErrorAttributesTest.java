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


package com.firefly.core.banking.psdx.web.error;

import com.firefly.core.banking.psdx.interfaces.dtos.PSDLinksDTO;
import com.firefly.core.banking.psdx.web.utils.LinkBuilder;
import com.firefly.core.banking.psdx.web.utils.LinkBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.http.server.RequestPath;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the CustomErrorAttributes.
 */
@ExtendWith(MockitoExtension.class)
class CustomErrorAttributesTest {

    @Mock
    private LinkBuilderFactory linkBuilderFactory;

    @Mock
    private LinkBuilder linkBuilder;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private ServerRequest.Headers headers;

    @Spy
    private DefaultErrorAttributes defaultErrorAttributes = new DefaultErrorAttributes();

    private CustomErrorAttributes customErrorAttributes;

    @BeforeEach
    void setUp() {
        when(linkBuilderFactory.create()).thenReturn(linkBuilder);
        when(linkBuilder.withSelf(any())).thenReturn(linkBuilder);
        when(linkBuilder.build()).thenReturn(new PSDLinksDTO());

        // Setup the spy to return the exception when getError is called
        doAnswer(invocation -> {
            ServerRequest request = invocation.getArgument(0);
            return request.exchange().getAttributes().get("org.springframework.boot.webflux.error.DefaultErrorAttributes.ERROR");
        }).when(defaultErrorAttributes).getError(any());

        customErrorAttributes = new CustomErrorAttributes(linkBuilderFactory) {
            @Override
            public Throwable getError(ServerRequest request) {
                return defaultErrorAttributes.getError(request);
            }
        };
    }

    @Test
    void getErrorAttributes_shouldAddCustomAttributes() {
        // Given
        when(linkBuilder.withSelf(any())).thenReturn(linkBuilder);
        when(linkBuilder.build()).thenReturn(new PSDLinksDTO());
        when(linkBuilderFactory.create()).thenReturn(linkBuilder);

        // Create a mock request with headers
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Request-ID", "request-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Add the exception to the exchange attributes
        RuntimeException testException = new RuntimeException("Test exception");
        exchange.getAttributes().put("org.springframework.boot.webflux.error.DefaultErrorAttributes.ERROR", testException);

        // Create a mock ServerRequest that returns our exchange
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.exchange()).thenReturn(exchange);
        ServerRequest.Headers mockHeaders = mock(ServerRequest.Headers.class);
        when(mockRequest.headers()).thenReturn(mockHeaders);
        when(mockHeaders.firstHeader("X-Request-ID")).thenReturn("request-id-123");
        RequestPath requestPath = RequestPath.parse(URI.create("/api/test"), "");
        when(mockRequest.requestPath()).thenReturn(requestPath);

        // When
        Map<String, Object> result = customErrorAttributes.getErrorAttributes(mockRequest, ErrorAttributeOptions.defaults());

        // Then
        assertNotNull(result);
        assertEquals("1.0", result.get("apiVersion"));
    }
}
