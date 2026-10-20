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

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filter for logging requests and detecting anomalies.
 * This filter logs all requests and detects potential anomalies such as:
 * - Too many requests from the same IP address
 * - Requests with suspicious headers
 * - Requests with suspicious parameters
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    private static final int REQUEST_THRESHOLD = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);
    
    private final ConcurrentMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

    /**
     * Filter method to log requests and detect anomalies.
     *
     * @param exchange The server web exchange
     * @param chain The web filter chain
     * @return A Mono of Void
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        
        log.debug("Request: {} {} from IP: {}, User-Agent: {}", method, path, ipAddress, userAgent);
        
        // Check for anomalies
        detectAnomalies(exchange, ipAddress);
        
        Instant start = Instant.now();
        
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("Response: {} {} - {} - {}ms", 
                            method, path, exchange.getResponse().getStatusCode(), duration.toMillis());
                });
    }

    /**
     * Detect anomalies in the request.
     *
     * @param exchange The server web exchange
     * @param ipAddress The IP address of the client
     */
    private void detectAnomalies(ServerWebExchange exchange, String ipAddress) {
        // Check for too many requests from the same IP address
        RequestCounter counter = requestCounters.computeIfAbsent(ipAddress, k -> new RequestCounter());
        int count = counter.incrementAndGet();
        
        if (count > REQUEST_THRESHOLD) {
            log.warn("Potential DoS attack detected from IP: {}, request count: {}", ipAddress, count);
        }
        
        // Check for suspicious headers
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.equals(ipAddress)) {
            log.warn("Suspicious X-Forwarded-For header: {}, IP: {}", xForwardedFor, ipAddress);
        }
        
        // Check for suspicious parameters
        exchange.getRequest().getQueryParams().forEach((key, values) -> {
            for (String value : values) {
                if (value.contains("<script>") || value.contains("javascript:") || value.contains("eval(")) {
                    log.warn("Potential XSS attack detected from IP: {}, parameter: {}, value: {}", 
                            ipAddress, key, value);
                }
                if (value.contains("'") || value.contains("--") || value.contains(";")) {
                    log.warn("Potential SQL injection attack detected from IP: {}, parameter: {}, value: {}", 
                            ipAddress, key, value);
                }
            }
        });
    }

    /**
     * Class for counting requests within a time window.
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private final Instant windowStart = Instant.now();
        
        /**
         * Increment the request count and return the new value.
         * If the time window has expired, reset the counter.
         *
         * @return The new request count
         */
        public int incrementAndGet() {
            if (Duration.between(windowStart, Instant.now()).compareTo(WINDOW_DURATION) > 0) {
                count.set(0);
            }
            return count.incrementAndGet();
        }
    }
}
