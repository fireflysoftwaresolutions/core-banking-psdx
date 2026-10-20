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


package io.fireflysoftware.core.banking.psdx.web.error;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDLinksDTO;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilder;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Spy
    private DefaultErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ServerCodecConfigurer serverCodecConfigurer;

    @Mock
    private LinkBuilderFactory linkBuilderFactory;

    @Mock
    private LinkBuilder linkBuilder;

    private WebProperties.Resources resources;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        resources = new WebProperties.Resources();

        // Create a real application context
        StaticApplicationContext appContext = new StaticApplicationContext();
        appContext.refresh();

        // Setup link builder mock with lenient stubbings
        lenient().when(linkBuilderFactory.create()).thenReturn(linkBuilder);
        lenient().when(linkBuilder.withSelf(any())).thenReturn(linkBuilder);
        lenient().when(linkBuilder.build()).thenReturn(new PSDLinksDTO());

        // Setup server codec configurer mock with lenient stubbings
        lenient().when(serverCodecConfigurer.getReaders()).thenReturn(Collections.emptyList());
        lenient().when(serverCodecConfigurer.getWriters()).thenReturn(Collections.emptyList());

        exceptionHandler = new GlobalExceptionHandler(
                errorAttributes,
                resources,
                appContext,
                serverCodecConfigurer,
                linkBuilderFactory
        );
    }

    private void setupErrorAttributesForException(Throwable exception) {
        // Setup the spy to return the exception when getError is called
        doReturn(exception).when(errorAttributes).getError(any());
    }

    @Test
    void getRoutingFunction_shouldHandleIllegalArgumentException() {
        try {
            // Given
            IllegalArgumentException exception = mock(IllegalArgumentException.class);
            when(exception.getMessage()).thenReturn("Invalid argument");

            // Create a mock request with the exception
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/test")
                    .header("X-Request-ID", "request-id-123")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Add the exception to the exchange attributes
            exchange.getAttributes().put("org.springframework.boot.webflux.error.DefaultErrorAttributes.ERROR", exception);

            // Setup the spy to return the exception when getError is called
            setupErrorAttributesForException(exception);

            // Create a real ServerRequest with our exchange
            ServerRequest serverRequest = ServerRequest.create(exchange, Collections.emptyList());

            // When
            Mono<ServerResponse> response = exceptionHandler.getRoutingFunction(errorAttributes)
                    .route(serverRequest)
                    .doOnNext(handler -> System.out.println("[DEBUG_LOG] Handler found for IllegalArgumentException: " + handler))
                    .doOnError(error -> System.out.println("[DEBUG_LOG] Error routing IllegalArgumentException request: " + error))
                    .flatMap(handler -> handler.handle(serverRequest)
                            .doOnNext(resp -> System.out.println("[DEBUG_LOG] Response for IllegalArgumentException: " + resp))
                            .doOnError(error -> System.out.println("[DEBUG_LOG] Error handling IllegalArgumentException request: " + error)));

            // Then
            StepVerifier.create(response)
                    .expectNextMatches(serverResponse ->
                            serverResponse.statusCode() == HttpStatus.BAD_REQUEST)
                    .verifyComplete();
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception caught in test: " + e);
            throw e;
        }
    }

    @Test
    void getRoutingFunction_shouldHandleResponseStatusException() {
        try {
            // Given
            ResponseStatusException exception = mock(ResponseStatusException.class);
            when(exception.getMessage()).thenReturn("Access denied");
            when(exception.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);
            when(exception.getReason()).thenReturn("Access denied");

            // Create a mock request with the exception
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/test")
                    .header("X-Request-ID", "request-id-123")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Add the exception to the exchange attributes
            exchange.getAttributes().put("org.springframework.boot.webflux.error.DefaultErrorAttributes.ERROR", exception);

            // Setup the spy to return the exception when getError is called
            setupErrorAttributesForException(exception);

            // Create a real ServerRequest with our exchange
            ServerRequest serverRequest = ServerRequest.create(exchange, Collections.emptyList());

            // When
            Mono<ServerResponse> response = exceptionHandler.getRoutingFunction(errorAttributes)
                    .route(serverRequest)
                    .doOnNext(handler -> System.out.println("[DEBUG_LOG] Handler found for ResponseStatusException: " + handler))
                    .doOnError(error -> System.out.println("[DEBUG_LOG] Error routing ResponseStatusException request: " + error))
                    .flatMap(handler -> handler.handle(serverRequest)
                            .doOnNext(resp -> System.out.println("[DEBUG_LOG] Response for ResponseStatusException: " + resp))
                            .doOnError(error -> System.out.println("[DEBUG_LOG] Error handling ResponseStatusException request: " + error)));

            // Then
            StepVerifier.create(response)
                    .expectNextMatches(serverResponse ->
                            serverResponse.statusCode() == HttpStatus.FORBIDDEN)
                    .verifyComplete();
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception caught in test: " + e);
            throw e;
        }
    }
}
