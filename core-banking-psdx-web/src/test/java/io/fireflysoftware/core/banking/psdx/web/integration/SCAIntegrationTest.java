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


package io.fireflysoftware.core.banking.psdx.web.integration;

import io.fireflysoftware.core.banking.psdx.core.ports.SCAServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationResponseDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationResponseDTO;
import io.fireflysoftware.core.banking.psdx.web.controllers.SCAController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the SCA API.
 */
@ExtendWith(MockitoExtension.class)
public class SCAIntegrationTest {

    private WebTestClient webTestClient;

    @Mock
    private SCAServicePort scaServicePort;

    @InjectMocks
    private SCAController scaController;

    // Test constants
    private static final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    private PSDSCAAuthenticationRequestDTO authRequest;
    private PSDSCAAuthenticationResponseDTO authResponse;
    private PSDSCAValidationRequestDTO validationRequest;
    private PSDSCAValidationResponseDTO validationResponse;

    @BeforeEach
    void setUp() {
        // Setup WebTestClient
        webTestClient = WebTestClient.bindToController(scaController).build();

        // Setup authentication request
        authRequest = new PSDSCAAuthenticationRequestDTO();
        authRequest.setPartyId(PARTY_ID);
        authRequest.setResourceId("payment-123456");
        authRequest.setResourceType("PAYMENT");
        authRequest.setAmount(100.00);
        authRequest.setCurrency("EUR");
        authRequest.setPreferredMethod("SMS");

        // Setup authentication response
        authResponse = new PSDSCAAuthenticationResponseDTO();
        authResponse.setChallengeId("sca-123456");
        authResponse.setMethod("SMS");
        authResponse.setMaskedTarget("+49 *** *** 789");
        authResponse.setExpiresIn(300);
        authResponse.setAdditionalInfo("An SMS has been sent to your registered mobile number");

        // Setup validation request
        validationRequest = new PSDSCAValidationRequestDTO();
        validationRequest.setChallengeId("sca-123456");
        validationRequest.setAuthenticationCode("123456");

        // Setup validation response
        validationResponse = new PSDSCAValidationResponseDTO();
        validationResponse.setSuccess(true);
        validationResponse.setAuthenticationToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        validationResponse.setExpiresIn(3600);
    }

    @Test
    void initiateSCA_shouldReturnAuthenticationResponse() {
        // Given
        when(scaServicePort.initiateSCA(any(PSDSCAAuthenticationRequestDTO.class))).thenReturn(Mono.just(authResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/sca/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDSCAAuthenticationResponseDTO.class)
                .isEqualTo(authResponse);
    }

    @Test
    void validateSCA_shouldReturnValidationResponse() {
        // Given
        when(scaServicePort.validateSCA(any(PSDSCAValidationRequestDTO.class))).thenReturn(Mono.just(validationResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/sca/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validationRequest)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDSCAValidationResponseDTO.class)
                .isEqualTo(validationResponse);
    }

    @Test
    void isSCARequired_shouldReturnBoolean() {
        // Given
        when(scaServicePort.isSCARequired(100.0, "EUR")).thenReturn(Mono.just(true));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/sca/required")
                        .queryParam("amount", "100.0")
                        .queryParam("currency", "EUR")
                        .build())
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }
}
