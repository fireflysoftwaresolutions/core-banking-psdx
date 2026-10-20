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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.models.entities.Consent;
import io.fireflysoftware.core.banking.psdx.models.repositories.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the ConsentValidationService.
 */
@ExtendWith(MockitoExtension.class)
class ConsentValidationServiceTest {

    @Mock
    private ConsentRepository consentRepository;

    @Mock
    private io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogService accessLogService;

    private ConsentValidationService consentValidationService;

    // Test constants
    private static final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID ACCOUNT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    private Consent validConsent;
    private Consent expiredConsent;
    private Consent invalidStatusConsent;
    private Consent limitedFrequencyConsent;

    @BeforeEach
    void setUp() {
        consentValidationService = new ConsentValidationService(consentRepository, accessLogService);

        LocalDateTime now = LocalDateTime.now();

        validConsent = new Consent();
        validConsent.setId(CONSENT_ID);
        validConsent.setPartyId(PARTY_ID);
        validConsent.setConsentType(ConsentType.ACCOUNT_INFORMATION);
        validConsent.setStatus(ConsentStatus.VALID);
        validConsent.setValidFrom(now.minusDays(1));
        validConsent.setValidUntil(now.plusDays(1));

        expiredConsent = new Consent();
        expiredConsent.setId(CONSENT_ID);
        expiredConsent.setPartyId(PARTY_ID);
        expiredConsent.setConsentType(ConsentType.ACCOUNT_INFORMATION);
        expiredConsent.setStatus(ConsentStatus.VALID);
        expiredConsent.setValidFrom(now.minusDays(2));
        expiredConsent.setValidUntil(now.minusDays(1));

        invalidStatusConsent = new Consent();
        invalidStatusConsent.setId(CONSENT_ID);
        invalidStatusConsent.setPartyId(PARTY_ID);
        invalidStatusConsent.setPartyId(UUID.fromString("550e8400-e29b-41d4-a716-446655440012"));
        invalidStatusConsent.setConsentType(ConsentType.ACCOUNT_INFORMATION);
        invalidStatusConsent.setStatus(ConsentStatus.REVOKED);
        invalidStatusConsent.setValidFrom(now.minusDays(1));
        invalidStatusConsent.setValidUntil(now.plusDays(1));

        limitedFrequencyConsent = new Consent();
        limitedFrequencyConsent.setId(CONSENT_ID);
        limitedFrequencyConsent.setPartyId(PARTY_ID);
        limitedFrequencyConsent.setConsentType(ConsentType.ACCOUNT_INFORMATION);
        limitedFrequencyConsent.setStatus(ConsentStatus.VALID);
        limitedFrequencyConsent.setValidFrom(now.minusDays(1));
        limitedFrequencyConsent.setValidUntil(now.plusDays(1));
        limitedFrequencyConsent.setAccessFrequency(5);
    }

    @Test
    void validateConsent_withValidConsent_shouldReturnTrue() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(validConsent));
        when(consentRepository.save(any(Consent.class))).thenReturn(Mono.just(validConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<Consent> consentCaptor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepository).save(consentCaptor.capture());
        assertNotNull(consentCaptor.getValue().getLastActionDate());
    }

    @Test
    void validateConsent_withExpiredConsent_shouldReturnFalse() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(expiredConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(consentRepository, never()).save(any(Consent.class));
    }

    @Test
    void validateConsent_withInvalidStatusConsent_shouldReturnFalse() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(invalidStatusConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(consentRepository, never()).save(any(Consent.class));
    }

    @Test
    void validateConsent_withWrongPartyId_shouldReturnFalse() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(validConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, ACCOUNT_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(consentRepository, never()).save(any(Consent.class));
    }

    @Test
    void validateConsent_withWrongResourceType_shouldReturnFalse() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(validConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.PAYMENT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(consentRepository, never()).save(any(Consent.class));
    }

    @Test
    void validateConsent_withLimitedFrequencyNotExceeded_shouldReturnTrue() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(limitedFrequencyConsent));
        when(accessLogService.countAccessLogsForConsent(CONSENT_ID)).thenReturn(Mono.just(3L));
        when(consentRepository.save(any(Consent.class))).thenReturn(Mono.just(limitedFrequencyConsent));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<Consent> consentCaptor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepository).save(consentCaptor.capture());
        assertNotNull(consentCaptor.getValue().getLastActionDate());
    }

    @Test
    void validateConsent_withLimitedFrequencyExceeded_shouldReturnFalse() {
        // Given
        when(consentRepository.findById(any(UUID.class))).thenReturn(Mono.just(limitedFrequencyConsent));
        when(accessLogService.countAccessLogsForConsent(CONSENT_ID)).thenReturn(Mono.just(5L));

        // When
        Mono<Boolean> result = consentValidationService.validateConsent(CONSENT_ID, ResourceType.ACCOUNT, PARTY_ID, "tpp1");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(consentRepository, never()).save(any(Consent.class));
    }
}
