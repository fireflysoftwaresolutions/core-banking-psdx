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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.web.controllers.ConsentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the Consent API.
 */
@ExtendWith(MockitoExtension.class)
public class ConsentIntegrationTest {

    private WebTestClient webTestClient;

    @Mock
    private ConsentService consentService;

    @InjectMocks
    private ConsentController consentController;

    // Test constants
    private static final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private PSDConsentDTO consentDTO;
    private PSDConsentRequestDTO consentRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup WebTestClient
        webTestClient = WebTestClient.bindToController(consentController).build();

        // Setup test data
        consentDTO = new PSDConsentDTO();
        consentDTO.setId(CONSENT_ID);
        consentDTO.setPartyId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        consentDTO.setConsentType("account");
        consentDTO.setConsentStatus("valid");
        consentDTO.setValidFrom(LocalDateTime.now().minusDays(1));
        consentDTO.setValidUntil(LocalDateTime.now().plusDays(90));
        consentDTO.setFrequencyPerDay(4);
        consentDTO.setRecurringIndicator(true);

        consentRequestDTO = new PSDConsentRequestDTO();
        consentRequestDTO.setPartyId(UUID.fromString("550e8400-e29b-41d4-a716-446655440017").toString());
        consentRequestDTO.setConsentType("account");
        consentRequestDTO.setValidFrom(LocalDateTime.now().minusDays(1));
        consentRequestDTO.setValidUntil(LocalDateTime.now().plusDays(90));
        consentRequestDTO.setFrequencyPerDay(4);
        consentRequestDTO.setRecurringIndicator(true);
        consentRequestDTO.setAccess(Arrays.asList(new PSDConsentRequestDTO.PSDAccessDTO()));
    }

    @Test
    void createConsent_shouldReturnCreatedConsent() {
        // Given
        when(consentService.createConsent(any(PSDConsentRequestDTO.class))).thenReturn(Mono.just(consentDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(consentRequestDTO)
                .header("X-Request-ID", "test-request-id")
                .header("PSU-ID", "test-psu-id")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(CONSENT_ID.toString())
                .jsonPath("$.partyId").isEqualTo("550e8400-e29b-41d4-a716-446655440001")
                .jsonPath("$.consentType").isEqualTo("account")
                .jsonPath("$.consentStatus").isEqualTo("valid")
                .jsonPath("$.recurringIndicator").isEqualTo(true);
    }

    @Test
    void getConsent_shouldReturnConsent() {
        // Given
        when(consentService.getConsent(CONSENT_ID)).thenReturn(Mono.just(consentDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/consents/" + CONSENT_ID)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(CONSENT_ID.toString())
                .jsonPath("$.partyId").isEqualTo("550e8400-e29b-41d4-a716-446655440001")
                .jsonPath("$.consentType").isEqualTo("account")
                .jsonPath("$.consentStatus").isEqualTo("valid")
                .jsonPath("$.recurringIndicator").isEqualTo(true);
    }

    @Test
    void getConsents_shouldReturnConsents() {
        // Given
        when(consentService.getConsentsForCustomer(any(UUID.class))).thenReturn(Flux.just(consentDTO));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/consents")
                        .queryParam("partyId", "550e8400-e29b-41d4-a716-446655440001")
                        .build())
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(CONSENT_ID.toString())
                .jsonPath("$[0].partyId").isEqualTo("550e8400-e29b-41d4-a716-446655440001")
                .jsonPath("$[0].consentType").isEqualTo("account")
                .jsonPath("$[0].consentStatus").isEqualTo("valid");
    }

    @Test
    void updateConsentStatus_shouldReturnUpdatedConsent() {
        // Given
        PSDConsentStatusDTO statusDTO = new PSDConsentStatusDTO();
        statusDTO.setConsentStatus("valid");

        // The controller returns a PSDConsentDTO, not a PSDConsentStatusDTO
        when(consentService.updateConsentStatus(eq(CONSENT_ID), any(PSDConsentStatusDTO.class))).thenReturn(Mono.just(consentDTO));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/consents/" + CONSENT_ID + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusDTO)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(CONSENT_ID.toString())
                .jsonPath("$.partyId").isEqualTo("550e8400-e29b-41d4-a716-446655440001")
                .jsonPath("$.consentType").isEqualTo("account")
                .jsonPath("$.consentStatus").isEqualTo("valid");
    }

    @Test
    void revokeConsent_shouldReturnNoContent() {
        // Given
        when(consentService.revokeConsent(CONSENT_ID)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/consents/" + CONSENT_ID)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk();
    }
}
