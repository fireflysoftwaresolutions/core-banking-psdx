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


package io.fireflysoftware.core.banking.psdx.core.services;

import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxAccountServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountReferenceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAmountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.models.entities.FundsConfirmation;
import io.fireflysoftware.core.banking.psdx.models.repositories.FundsConfirmationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationServiceImplTest {

    @Mock
    private AccountServicePort accountServicePort;

    @Mock
    private ConsentService consentService;

    @Mock
    private FundsConfirmationRepository fundsConfirmationRepository;

    @InjectMocks
    private FundsConfirmationServiceImpl fundsConfirmationService;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID FUNDS_CONFIRMATION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

    private PSDFundsConfirmationDTO fundsConfirmationRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        PSDAccountReferenceDTO account = new PSDAccountReferenceDTO();
        account.setIban("DE89370400440532013000");

        PSDAmountDTO amount = new PSDAmountDTO();
        amount.setCurrency("EUR");
        amount.setAmount(BigDecimal.valueOf(100.00));

        fundsConfirmationRequest = new PSDFundsConfirmationDTO();
        fundsConfirmationRequest.setAccount(account);
        fundsConfirmationRequest.setInstructedAmount(amount);
    }

    @Test
    void confirmFunds_shouldReturnConfirmation_whenConsentIsValid() {
        when(consentService.validateConsent(CONSENT_ID, "FUNDS_CONFIRMATION", "READ")).thenReturn(Mono.just(true));
        when(accountServicePort.balancesForReference("DE89370400440532013000"))
                .thenReturn(Flux.just(SandboxAccountServiceAdapter.balance(new BigDecimal("125.00"))));
        when(fundsConfirmationRepository.save(any(FundsConfirmation.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(fundsConfirmationService.confirmFunds(CONSENT_ID, fundsConfirmationRequest))
                .expectNextMatches(confirmation -> confirmation.getFundsConfirmationId() != null
                        && confirmation.getConsentId().equals(CONSENT_ID)
                        && Boolean.TRUE.equals(confirmation.getFundsAvailable())
                        && confirmation.getConfirmationDateTime() != null)
                .verifyComplete();
    }

    @Test
    void confirmFunds_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "FUNDS_CONFIRMATION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(fundsConfirmationService.confirmFunds(CONSENT_ID, fundsConfirmationRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getFundsConfirmation_shouldReturnConfirmation_whenConsentIsValid() {
        when(consentService.validateConsent(CONSENT_ID, "FUNDS_CONFIRMATION", "READ")).thenReturn(Mono.just(true));
        when(fundsConfirmationRepository.findById(FUNDS_CONFIRMATION_ID)).thenReturn(Mono.just(FundsConfirmation.builder()
                .id(FUNDS_CONFIRMATION_ID)
                .consentId(CONSENT_ID)
                .fundsAvailable(true)
                .confirmationDateTime(LocalDateTime.parse("2026-01-15T09:00:00"))
                .build()));

        StepVerifier.create(fundsConfirmationService.getFundsConfirmation(CONSENT_ID, FUNDS_CONFIRMATION_ID))
                .expectNextMatches(confirmation -> confirmation.getFundsConfirmationId().equals(FUNDS_CONFIRMATION_ID)
                        && confirmation.getConsentId().equals(CONSENT_ID)
                        && Boolean.TRUE.equals(confirmation.getFundsAvailable())
                        && confirmation.getConfirmationDateTime() != null)
                .verifyComplete();
    }

    @Test
    void getFundsConfirmation_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "FUNDS_CONFIRMATION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(fundsConfirmationService.getFundsConfirmation(CONSENT_ID, FUNDS_CONFIRMATION_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
