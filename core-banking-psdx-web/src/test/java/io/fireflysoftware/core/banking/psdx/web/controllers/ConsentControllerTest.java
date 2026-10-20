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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentControllerTest {

    @Mock
    private ConsentService consentService;

    @InjectMocks
    private ConsentController consentController;

    private WebTestClient webTestClient;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    private PSDConsentDTO consent1;
    private PSDConsentDTO consent2;
    private PSDConsentRequestDTO consentRequest;
    private PSDConsentStatusDTO statusUpdate;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(consentController).build();

        // Setup test data
        consent1 = new PSDConsentDTO();
        consent1.setId(CONSENT_ID);
        consent1.setPartyId(PARTY_ID);
        consent1.setConsentType("account");
        consent1.setConsentStatus("VALID");
        consent1.setValidFrom(LocalDateTime.now().withNano(0));
        consent1.setValidUntil(LocalDateTime.now().plusDays(90).withNano(0));
        consent1.setFrequencyPerDay(4);
        consent1.setCreatedAt(LocalDateTime.now().withNano(0));

        consent2 = new PSDConsentDTO();
        consent2.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"));
        consent2.setPartyId(PARTY_ID);
        consent2.setConsentType("payment");
        consent2.setConsentStatus("VALID");
        consent2.setValidFrom(LocalDateTime.now().withNano(0));
        consent2.setValidUntil(LocalDateTime.now().plusDays(90).withNano(0));
        consent2.setFrequencyPerDay(4);
        consent2.setCreatedAt(LocalDateTime.now().withNano(0));

        consentRequest = new PSDConsentRequestDTO();
        consentRequest.setPartyId(PARTY_ID.toString());
        consentRequest.setConsentType("account");
        consentRequest.setValidUntil(LocalDateTime.now().plusDays(90).withNano(0));
        consentRequest.setValidFrom(LocalDateTime.now().withNano(0));
        consentRequest.setFrequencyPerDay(4);
        consentRequest.setRecurringIndicator(true);
        consentRequest.setCombinedServiceIndicator(false);

        // Create access permissions
        List<PSDConsentRequestDTO.PSDAccessDTO> accessList = new ArrayList<>();
        PSDConsentRequestDTO.PSDAccessDTO access = new PSDConsentRequestDTO.PSDAccessDTO();
        access.setType("account");

        // Create account references
        List<PSDConsentRequestDTO.PSDAccountReferenceDTO> accounts = new ArrayList<>();
        PSDConsentRequestDTO.PSDAccountReferenceDTO account = new PSDConsentRequestDTO.PSDAccountReferenceDTO();
        account.setIban("DE89370400440532013000");
        account.setCurrency("EUR");
        accounts.add(account);

        access.setAccounts(accounts);
        accessList.add(access);
        consentRequest.setAccess(accessList);

        statusUpdate = new PSDConsentStatusDTO();
        statusUpdate.setConsentStatus("revoked");
        statusUpdate.setStatus("revoked");
        statusUpdate.setStatusUpdateDateTime(LocalDateTime.now().withNano(0));
    }

    @Test
    void createConsent_shouldReturnCreatedConsent() {
        // Given
        when(consentService.createConsent(any(PSDConsentRequestDTO.class)))
                .thenReturn(Mono.just(consent1));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(consentRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PSDConsentDTO.class)
                .isEqualTo(consent1);
    }

    @Test
    void getConsent_shouldReturnConsent() {
        // Given
        when(consentService.getConsent(CONSENT_ID))
                .thenReturn(Mono.just(consent1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/consents/{consentId}", CONSENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDConsentDTO.class)
                .isEqualTo(consent1);
    }

    @Test
    void getConsentsForCustomer_shouldReturnConsents() {
        // Given
        when(consentService.getConsentsForCustomer(PARTY_ID))
                .thenReturn(Flux.fromIterable(Arrays.asList(consent1, consent2)));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/consents")
                        .queryParam("partyId", PARTY_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PSDConsentDTO.class)
                .hasSize(2)
                .contains(consent1, consent2);
    }

    @Test
    void updateConsentStatus_shouldReturnUpdatedConsent() {
        // Given
        PSDConsentDTO updatedConsent = new PSDConsentDTO();
        updatedConsent.setId(CONSENT_ID);
        updatedConsent.setPartyId(PARTY_ID);
        updatedConsent.setConsentType("ACCOUNT_INFORMATION");
        updatedConsent.setConsentStatus("revoked");
        updatedConsent.setValidFrom(LocalDateTime.now().withNano(0));
        updatedConsent.setValidUntil(LocalDateTime.now().plusDays(90).withNano(0));
        updatedConsent.setFrequencyPerDay(4);
        updatedConsent.setCreatedAt(LocalDateTime.now().withNano(0));

        when(consentService.updateConsentStatus(eq(CONSENT_ID), any(PSDConsentStatusDTO.class)))
                .thenReturn(Mono.just(updatedConsent));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/consents/{consentId}/status", CONSENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDConsentDTO.class)
                .isEqualTo(updatedConsent);
    }

    @Test
    void revokeConsent_shouldReturnRevokedConsent() {
        // Given
        PSDConsentDTO revokedConsent = new PSDConsentDTO();
        revokedConsent.setId(CONSENT_ID);
        revokedConsent.setPartyId(PARTY_ID);
        revokedConsent.setConsentType("ACCOUNT_INFORMATION");
        revokedConsent.setConsentStatus("REVOKED");
        revokedConsent.setValidFrom(LocalDateTime.now().withNano(0));
        revokedConsent.setValidUntil(LocalDateTime.now().plusDays(90).withNano(0));
        revokedConsent.setFrequencyPerDay(4);
        revokedConsent.setCreatedAt(LocalDateTime.now().withNano(0));

        when(consentService.revokeConsent(CONSENT_ID))
                .thenReturn(Mono.just(revokedConsent));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/consents/{consentId}", CONSENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDConsentDTO.class)
                .isEqualTo(revokedConsent);
    }
}
