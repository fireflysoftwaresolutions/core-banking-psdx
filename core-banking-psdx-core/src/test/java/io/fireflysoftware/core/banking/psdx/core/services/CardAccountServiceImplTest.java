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

import io.fireflysoftware.core.banking.psdx.core.ports.CardServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDCardAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardAccountServiceImplTest {

    @Mock
    private CardServicePort cardServicePort;

    @Mock
    private ConsentService consentService;

    @InjectMocks
    private CardAccountServiceImpl cardAccountService;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID CARD_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    private final UUID TRANSACTION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

    private PSDCardAccountDTO card1;
    private PSDCardAccountDTO card2;
    private PSDBalanceDTO balance1;
    private PSDBalanceDTO balance2;
    private PSDTransactionDTO transaction1;
    private PSDTransactionDTO transaction2;

    @BeforeEach
    void setUp() {
        // Setup test data
        card1 = new PSDCardAccountDTO();
        card1.setResourceId(CARD_ID);
        card1.setMaskedPan("540905******0000");
        card1.setOwnerPartyId(PARTY_ID);

        card2 = new PSDCardAccountDTO();
        card2.setResourceId(UUID.fromString("550e8400-e29b-41d4-a716-446655440014"));
        card2.setMaskedPan("540905******0001");
        card2.setOwnerPartyId(PARTY_ID);

        balance1 = new PSDBalanceDTO();
        balance1.setBalanceType("closingBooked");
        balance1.setBalanceAmount(new PSDBalanceDTO.PSDAmountDTO("EUR", java.math.BigDecimal.valueOf(1000.00)));

        balance2 = new PSDBalanceDTO();
        balance2.setBalanceType("expected");
        balance2.setBalanceAmount(new PSDBalanceDTO.PSDAmountDTO("EUR", java.math.BigDecimal.valueOf(1100.00)));

        transaction1 = new PSDTransactionDTO();
        transaction1.setTransactionId(TRANSACTION_ID);
        transaction1.setTransactionStatus("booked");
        transaction1.setBookingDate(LocalDate.now().minusDays(1));

        transaction2 = new PSDTransactionDTO();
        transaction2.setTransactionId(UUID.fromString("550e8400-e29b-41d4-a716-446655440015"));
        transaction2.setTransactionStatus("booked");
        transaction2.setBookingDate(LocalDate.now().minusDays(2));
    }

    @Test
    void getCardAccounts_shouldReturnCards_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD", "READ")).thenReturn(Mono.just(true));
        when(cardServicePort.getCardAccountsByPartyId(PARTY_ID)).thenReturn(Flux.fromIterable(Arrays.asList(card1, card2)));

        // When & Then
        StepVerifier.create(cardAccountService.getCardAccounts(CONSENT_ID, PARTY_ID))
                .expectNext(card1)
                .expectNext(card2)
                .verifyComplete();
    }

    @Test
    void getCardAccounts_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(cardAccountService.getCardAccounts(CONSENT_ID, PARTY_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getCardAccount_shouldReturnCard_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD", "READ")).thenReturn(Mono.just(true));
        when(cardServicePort.getCardAccountById(CARD_ID)).thenReturn(Mono.just(card1));

        // When & Then
        StepVerifier.create(cardAccountService.getCardAccount(CONSENT_ID, CARD_ID))
                .expectNext(card1)
                .verifyComplete();
    }

    @Test
    void getCardAccount_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(cardAccountService.getCardAccount(CONSENT_ID, CARD_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getCardBalances_shouldReturnBalances_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD_BALANCE", "READ")).thenReturn(Mono.just(true));
        when(cardServicePort.getBalancesByCardId(CARD_ID)).thenReturn(Flux.fromIterable(Arrays.asList(balance1, balance2)));

        // When & Then
        StepVerifier.create(cardAccountService.getCardBalances(CONSENT_ID, CARD_ID))
                .expectNext(balance1)
                .expectNext(balance2)
                .verifyComplete();
    }

    @Test
    void getCardBalances_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD_BALANCE", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(cardAccountService.getCardBalances(CONSENT_ID, CARD_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getCardTransactions_shouldReturnTransactions_whenConsentIsValid() {
        // Given
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        when(consentService.validateConsent(CONSENT_ID, "CARD_TRANSACTION", "READ")).thenReturn(Mono.just(true));
        when(cardServicePort.getTransactionsByCardId(CARD_ID, fromDate, toDate))
                .thenReturn(Flux.fromIterable(Arrays.asList(transaction1, transaction2)));

        // When & Then
        StepVerifier.create(cardAccountService.getCardTransactions(CONSENT_ID, CARD_ID, fromDate, toDate))
                .expectNext(transaction1)
                .expectNext(transaction2)
                .verifyComplete();
    }

    @Test
    void getCardTransactions_shouldReturnError_whenConsentIsInvalid() {
        // Given
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        when(consentService.validateConsent(CONSENT_ID, "CARD_TRANSACTION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(cardAccountService.getCardTransactions(CONSENT_ID, CARD_ID, fromDate, toDate))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getCardTransaction_shouldReturnTransaction_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD_TRANSACTION", "READ")).thenReturn(Mono.just(true));
        when(cardServicePort.getTransactionByCardIdAndTransactionId(CARD_ID, TRANSACTION_ID)).thenReturn(Mono.just(transaction1));

        // When & Then
        StepVerifier.create(cardAccountService.getCardTransaction(CONSENT_ID, CARD_ID, TRANSACTION_ID))
                .expectNext(transaction1)
                .verifyComplete();
    }

    @Test
    void getCardTransaction_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "CARD_TRANSACTION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(cardAccountService.getCardTransaction(CONSENT_ID, CARD_ID, TRANSACTION_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
