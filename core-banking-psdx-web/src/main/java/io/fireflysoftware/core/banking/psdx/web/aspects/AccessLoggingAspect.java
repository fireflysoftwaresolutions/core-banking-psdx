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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Aspect for logging all API accesses.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessLoggingAspect {

    private final AccessLogServiceInterface accessLogService;

    /**
     * Around advice for logging all controller method calls.
     *
     * @param joinPoint The join point
     * @return The result of the method call
     * @throws Throwable If an error occurs
     */
    @Around("execution(* io.fireflysoftware.core.banking.psdx.web.controllers.*.*(..))")
    public Object logAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Skip access logging for access log controller methods to avoid infinite loops
        if (method.getDeclaringClass().getSimpleName().equals("AccessLogController")) {
            return joinPoint.proceed();
        }
        
        // Get request mapping annotations
        RequestMapping classMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
        String classPath = classMapping != null && classMapping.value().length > 0 ? classMapping.value()[0] : "";
        
        String methodPath = "";
        AccessType accessType = AccessType.READ;
        
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            accessType = AccessType.READ;
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            accessType = AccessType.WRITE;
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            accessType = AccessType.WRITE;
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            accessType = AccessType.DELETE;
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping mapping = method.getAnnotation(PatchMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            accessType = AccessType.WRITE;
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
            if (mapping.method().length > 0) {
                switch (mapping.method()[0]) {
                    case GET:
                        accessType = AccessType.READ;
                        break;
                    case POST:
                    case PUT:
                    case PATCH:
                        accessType = AccessType.WRITE;
                        break;
                    case DELETE:
                        accessType = AccessType.DELETE;
                        break;
                }
            }
        }
        
        String path = classPath + methodPath;
        
        // Get resource type from path
        ResourceType resourceType = getResourceTypeFromPath(path);
        
        // Get resource ID from path or parameters
        String resourceId = getResourceIdFromPath(path);
        
        // Get request information
        ServerWebExchange exchange = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ServerWebExchange) {
                exchange = (ServerWebExchange) arg;
                break;
            }
        }
        
        if (exchange == null) {
            // If no ServerWebExchange is found, proceed without logging
            return joinPoint.proceed();
        }
        
        ServerHttpRequest request = exchange.getRequest();
        String ipAddress = request.getRemoteAddress().getAddress().getHostAddress();
        String userAgent = request.getHeaders().getFirst("User-Agent");
        
        // Get consent ID from header
        String consentIdHeader = request.getHeaders().getFirst("X-Consent-ID");
        UUID consentId = null;
        if (consentIdHeader != null && !consentIdHeader.isEmpty()) {
            try {
                consentId = UUID.fromString(consentIdHeader);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid X-Consent-ID header: {}", consentIdHeader);
            }
        }

        // Get party ID from request parameter or header
        UUID partyId = null;
        String partyIdParam = request.getQueryParams().getFirst("partyId");
        if (partyIdParam != null && !partyIdParam.isEmpty()) {
            try {
                partyId = UUID.fromString(partyIdParam);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid party ID parameter: {}", partyIdParam);
            }
        }
        
        // Get third party ID from API key header
        String thirdPartyId = request.getHeaders().getFirst("X-API-KEY");
        
        // Get request ID headers
        String xRequestId = request.getHeaders().getFirst("X-Request-ID");
        String tppRequestId = request.getHeaders().getFirst("TPP-Request-ID");
        
        // Get PSU headers
        String psuId = request.getHeaders().getFirst("PSU-ID");
        
        try {
            // Proceed with the method call
            Object result = joinPoint.proceed();
            
            // Log successful access
            if (consentId != null && partyId != null && thirdPartyId != null) {
                accessLogService.logAccess(
                        consentId,
                        partyId,
                        thirdPartyId,
                        accessType,
                        resourceType,
                        resourceId,
                        ipAddress,
                        userAgent,
                        AccessStatus.SUCCESS,
                        xRequestId,
                        tppRequestId,
                        psuId
                ).subscribe();
            }
            
            return result;
        } catch (Exception e) {
            // Log failed access
            if (consentId != null && partyId != null && thirdPartyId != null) {
                accessLogService.logAccess(
                        consentId,
                        partyId,
                        thirdPartyId,
                        accessType,
                        resourceType,
                        resourceId,
                        ipAddress,
                        userAgent,
                        AccessStatus.ERROR,
                        xRequestId,
                        tppRequestId,
                        psuId
                ).subscribe();
            }
            
            throw e;
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
        } else if (path.contains("/consents")) {
            return ResourceType.CONSENT;
        } else {
            return ResourceType.CONSENT;
        }
    }

    /**
     * Get the resource ID from the path.
     *
     * @param path The path
     * @return The resource ID
     */
    private String getResourceIdFromPath(String path) {
        // Extract resource ID from path segments
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].equals("accounts") || 
                segments[i].equals("card-accounts") || 
                segments[i].equals("payments") || 
                segments[i].equals("funds-confirmations") || 
                segments[i].equals("consents")) {
                if (i + 1 < segments.length && !segments[i + 1].isEmpty() && !segments[i + 1].contains("{")) {
                    return segments[i + 1];
                }
            }
        }
        
        return "";
    }
}
