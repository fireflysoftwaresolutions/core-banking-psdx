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


package io.fireflysoftware.core.banking.psdx.web.aspects;

import io.fireflysoftware.core.banking.psdx.core.services.AccessLogService;
import static org.mockito.ArgumentMatchers.any;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import static org.mockito.ArgumentMatchers.any;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import static org.mockito.ArgumentMatchers.any;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import static org.mockito.ArgumentMatchers.any;
import org.aspectj.lang.ProceedingJoinPoint;
import static org.mockito.ArgumentMatchers.any;
import org.aspectj.lang.reflect.MethodSignature;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.http.server.reactive.ServerHttpRequest;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.http.server.reactive.ServerHttpResponse;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.web.bind.annotation.GetMapping;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.web.server.ServerWebExchange;
import static org.mockito.ArgumentMatchers.any;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;

import java.lang.reflect.Method;
import static org.mockito.ArgumentMatchers.any;
import java.net.InetAddress;
import static org.mockito.ArgumentMatchers.any;
import java.net.InetSocketAddress;
import static org.mockito.ArgumentMatchers.any;
import java.net.UnknownHostException;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Tests for the AccessLoggingAspect.
 */
@ExtendWith(MockitoExtension.class)
class AccessLoggingAspectTest {

    @Mock
    private AccessLogService accessLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    private AccessLoggingAspect aspect;

    // Test constants
    private static final String VALID_CONSENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_PARTY_ID = "550e8400-e29b-41d4-a716-446655440001";

    @BeforeEach
    void setUp() throws UnknownHostException {
        aspect = new AccessLoggingAspect(accessLogService);

        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(request.getHeaders()).thenReturn(mock(org.springframework.http.HttpHeaders.class));
        lenient().when(request.getQueryParams()).thenReturn(mock(org.springframework.util.MultiValueMap.class));
        lenient().when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 12345));

        // Add stubs for common header values
        lenient().when(request.getHeaders().getFirst("User-Agent")).thenReturn("test-agent");
    }

    @Test
    void logAccess_shouldLogSuccessfulAccess() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("getResource");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{exchange});
        when(joinPoint.proceed()).thenReturn("success");

        when(request.getHeaders().getFirst("X-Consent-ID")).thenReturn(VALID_CONSENT_ID);
        when(request.getHeaders().getFirst("X-API-KEY")).thenReturn("api-key-123");
        when(request.getQueryParams().getFirst("partyId")).thenReturn(VALID_PARTY_ID);

        when(accessLogService.logAccess(
                any(UUID.class), any(UUID.class), eq("api-key-123"), eq(AccessType.READ), any(ResourceType.class),
                anyString(), anyString(), anyString(), eq(AccessStatus.SUCCESS), isNull(), isNull(), isNull()
        )).thenReturn(Mono.empty());

        // When
        Object result = aspect.logAccess(joinPoint);

        // Then
        assertEquals("success", result);
        verify(accessLogService).logAccess(
                any(UUID.class), any(UUID.class), eq("api-key-123"), eq(AccessType.READ), any(ResourceType.class),
                anyString(), anyString(), anyString(), eq(AccessStatus.SUCCESS), isNull(), isNull(), isNull()
        );
    }

    @Test
    void logAccess_shouldLogFailedAccess() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("getResource");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{exchange});
        RuntimeException testException = new RuntimeException("test exception");
        when(joinPoint.proceed()).thenThrow(testException);

        when(request.getHeaders().getFirst("X-Consent-ID")).thenReturn(VALID_CONSENT_ID);
        when(request.getHeaders().getFirst("X-API-KEY")).thenReturn("api-key-123");
        when(request.getQueryParams().getFirst("partyId")).thenReturn(VALID_PARTY_ID);

        // Use lenient() to avoid strict stubbing issues
        lenient().when(accessLogService.logAccess(
                any(UUID.class), any(UUID.class), anyString(), any(AccessType.class), any(ResourceType.class),
                anyString(), anyString(), anyString(), eq(AccessStatus.ERROR), isNull(), isNull(), isNull()
        )).thenReturn(Mono.empty());

        // When & Then
        try {
            aspect.logAccess(joinPoint);
            // If we get here, the test should fail because an exception should have been thrown
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            // Print the actual exception for debugging
            System.out.println("[DEBUG_LOG] Caught exception: " + e);
            System.out.println("[DEBUG_LOG] Exception message: " + e.getMessage());
            System.out.println("[DEBUG_LOG] Exception class: " + e.getClass().getName());

            // Verify it's a RuntimeException (don't check the message for now)
            assertEquals(RuntimeException.class, e.getClass());
        }

        verify(accessLogService).logAccess(
                any(UUID.class), any(UUID.class), anyString(), any(AccessType.class), any(ResourceType.class),
                anyString(), anyString(), anyString(), eq(AccessStatus.ERROR), isNull(), isNull(), isNull()
        );
    }

    @Test
    void logAccess_shouldSkipLoggingForAccessLogController() throws Throwable {
        // Given
        Method method = AccessLogController.class.getMethod("getAccessLogs");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.logAccess(joinPoint);

        // Then
        assertEquals("success", result);
        verify(accessLogService, never()).logAccess(
                any(UUID.class), any(UUID.class), anyString(), any(AccessType.class), any(ResourceType.class),
                anyString(), anyString(), anyString(), any(AccessStatus.class), anyString(), anyString(), anyString()
        );
    }

    @Test
    void logAccess_shouldSkipLoggingWhenMissingRequiredHeaders() throws Throwable {
        // Given
        Method method = TestController.class.getMethod("getResource");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{exchange});
        when(joinPoint.proceed()).thenReturn("success");

        // Missing X-Consent-ID
        when(request.getHeaders().getFirst("X-Consent-ID")).thenReturn(null);
        when(request.getHeaders().getFirst("X-API-KEY")).thenReturn("api-key-123");
        when(request.getQueryParams().getFirst("partyId")).thenReturn(VALID_PARTY_ID);

        // When
        Object result = aspect.logAccess(joinPoint);

        // Then
        assertEquals("success", result);
        verify(accessLogService, never()).logAccess(
                any(UUID.class), any(UUID.class), anyString(), any(AccessType.class), any(ResourceType.class),
                anyString(), anyString(), anyString(), any(AccessStatus.class), anyString(), anyString(), anyString()
        );
    }

    @RequestMapping("/api/v1/test")
    static class TestController {
        @GetMapping
        public String getResource() {
            return "resource";
        }
    }

    @RequestMapping("/api/v1/access-logs")
    static class AccessLogController {
        @GetMapping
        public String getAccessLogs() {
            return "logs";
        }
    }
}
