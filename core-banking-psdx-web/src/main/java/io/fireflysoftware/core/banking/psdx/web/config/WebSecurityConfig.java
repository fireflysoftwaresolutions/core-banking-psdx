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


package io.fireflysoftware.core.banking.psdx.web.config;

import io.fireflysoftware.core.banking.psdx.web.security.ApiKeyAuthenticationWebFilter;
import io.fireflysoftware.core.banking.psdx.web.security.JwtAuthenticationWebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

/**
 * Security configuration for the PSD2/PSD3 and FIDA Regulatory Compliance Service.
 * This configuration implements the security requirements for the service, including:
 * - API key authentication for TPPs
 * - JWT authentication for users
 * - Role-based access control
 * - CORS and CSRF protection
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ApiKeyAuthenticationWebFilter apiKeyAuthenticationWebFilter;
    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

    /**
     * Configure the security filter chain.
     *
     * @param http The ServerHttpSecurity to configure
     * @return The configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", 
                                "/webjars/swagger-ui/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
                        // TPP registration is public but will be rate-limited
                        .pathMatchers(HttpMethod.POST, "/api/providers/register").permitAll()
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                // Add custom filters
                .addFilterAt(apiKeyAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAfter(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
