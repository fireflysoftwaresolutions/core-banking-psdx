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


package io.fireflysoftware.core.banking.psdx.web.controllers;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAuthenticationResponseDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDRefreshTokenRequestDTO;
import io.fireflysoftware.core.banking.psdx.web.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for authentication")
public class AuthenticationController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate a user and generate JWT tokens.
     *
     * @param request The authentication request
     * @return A Mono of PSDAuthenticationResponseDTO
     */
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authenticate", description = "Authenticate a user and generate JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = PSDAuthenticationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDAuthenticationResponseDTO> authenticate(
            @Parameter(description = "Authentication request", required = true)
            @Valid @RequestBody PSDAuthenticationRequestDTO request) {
        log.debug("REST request to authenticate user: {}", request.getUsername());

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                .map(this::createTokenResponse);
    }

    /**
     * Refresh a JWT token.
     *
     * @param request The refresh token request
     * @return A Mono of PSDAuthenticationResponseDTO
     */
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Refresh token", description = "Refresh a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = PSDAuthenticationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDAuthenticationResponseDTO> refreshToken(
            @Parameter(description = "Refresh token request", required = true)
            @Valid @RequestBody PSDRefreshTokenRequestDTO request) {
        log.debug("REST request to refresh token");

        // Validate refresh token and generate new tokens
        // This is a simplified implementation
        if (jwtTokenProvider.validateToken(request.getRefreshToken())) {
            Authentication authentication = jwtTokenProvider.getAuthentication(request.getRefreshToken());
            return Mono.just(createTokenResponse(authentication));
        }

        return Mono.error(new RuntimeException("Invalid refresh token"));
    }

    /**
     * Create a token response from an authentication.
     *
     * @param authentication The authentication
     * @return The token response
     */
    private PSDAuthenticationResponseDTO createTokenResponse(Authentication authentication) {
        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return PSDAuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
