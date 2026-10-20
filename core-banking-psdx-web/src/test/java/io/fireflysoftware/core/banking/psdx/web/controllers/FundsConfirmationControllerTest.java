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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountReferenceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAmountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.FundsConfirmationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationControllerTest {

    @Mock
    private FundsConfirmationService fundsConfirmationService;

    @InjectMocks
    private FundsConfirmationController fundsConfirmationController;

    private WebTestClient webTestClient;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID FUNDS_CONFIRMATION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

    private PSDFundsConfirmationDTO fundsConfirmation;
    private PSDFundsConfirmationDTO fundsConfirmationRequest;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(fundsConfirmationController).build();

        // Setup test data
        fundsConfirmation = new PSDFundsConfirmationDTO();
        fundsConfirmation.setFundsConfirmationId(FUNDS_CONFIRMATION_ID);
        fundsConfirmation.setConsentId(CONSENT_ID);

        // Create account reference
        PSDAccountReferenceDTO account = new PSDAccountReferenceDTO();
        account.setIban("DE89370400440532013000");
        account.setCurrency("EUR");
        fundsConfirmation.setAccount(account);

        // Create instructed amount
        PSDAmountDTO instructedAmount = new PSDAmountDTO();
        instructedAmount.setCurrency("EUR");
        instructedAmount.setAmount(BigDecimal.valueOf(100.00));
        fundsConfirmation.setInstructedAmount(instructedAmount);

        fundsConfirmation.setCreditorName("John Doe");

        // Create creditor account
        PSDAccountReferenceDTO creditorAccount = new PSDAccountReferenceDTO();
        creditorAccount.setIban("FR7630006000011234567890189");
        creditorAccount.setCurrency("EUR");
        fundsConfirmation.setCreditorAccount(creditorAccount);

        fundsConfirmation.setFundsAvailable(true);
        fundsConfirmation.setConfirmationDateTime(LocalDateTime.now().withNano(0));

        fundsConfirmationRequest = new PSDFundsConfirmationDTO();

        // Create account reference for request
        PSDAccountReferenceDTO accountRequest = new PSDAccountReferenceDTO();
        accountRequest.setIban("DE89370400440532013000");
        accountRequest.setCurrency("EUR");
        fundsConfirmationRequest.setAccount(accountRequest);

        // Create instructed amount for request
        PSDAmountDTO instructedAmountRequest = new PSDAmountDTO();
        instructedAmountRequest.setCurrency("EUR");
        instructedAmountRequest.setAmount(BigDecimal.valueOf(100.00));
        fundsConfirmationRequest.setInstructedAmount(instructedAmountRequest);

        fundsConfirmationRequest.setCreditorName("John Doe");

        // Create creditor account for request
        PSDAccountReferenceDTO creditorAccountRequest = new PSDAccountReferenceDTO();
        creditorAccountRequest.setIban("FR7630006000011234567890189");
        creditorAccountRequest.setCurrency("EUR");
        fundsConfirmationRequest.setCreditorAccount(creditorAccountRequest);
    }

    @Test
    void confirmFunds_shouldReturnFundsConfirmation() {
        // Given
        when(fundsConfirmationService.confirmFunds(eq(CONSENT_ID), any(PSDFundsConfirmationDTO.class)))
                .thenReturn(Mono.just(fundsConfirmation));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/funds-confirmations")
                .header("X-Request-ID", "test-request-id")
                .header("X-Consent-ID", CONSENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fundsConfirmationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PSDFundsConfirmationDTO.class)
                .isEqualTo(fundsConfirmation);
    }

    @Test
    void getFundsConfirmation_shouldReturnFundsConfirmation() {
        // Given
        when(fundsConfirmationService.getFundsConfirmation(CONSENT_ID, FUNDS_CONFIRMATION_ID))
                .thenReturn(Mono.just(fundsConfirmation));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/funds-confirmations/{fundsConfirmationId}", FUNDS_CONFIRMATION_ID)
                .header("X-Consent-ID", CONSENT_ID.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDFundsConfirmationDTO.class)
                .isEqualTo(fundsConfirmation);
    }
}
