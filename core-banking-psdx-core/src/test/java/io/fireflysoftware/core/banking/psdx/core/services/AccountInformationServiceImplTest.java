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

import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.banking.psdx.core.ports.TransactionServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
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
class AccountInformationServiceImplTest {

    @Mock
    private AccountServicePort accountServicePort;

    @Mock
    private TransactionServicePort transactionServicePort;

    @Mock
    private ConsentService consentService;

    @InjectMocks
    private AccountInformationServiceImpl accountInformationService;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID ACCOUNT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private final UUID TRANSACTION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

    private PSDAccountDTO account1;
    private PSDAccountDTO account2;
    private PSDBalanceDTO balance1;
    private PSDBalanceDTO balance2;
    private PSDTransactionDTO transaction1;
    private PSDTransactionDTO transaction2;

    @BeforeEach
    void setUp() {
        // Setup test data
        account1 = new PSDAccountDTO();
        account1.setResourceId(ACCOUNT_ID);
        account1.setIban("DE89370400440532013000");
        account1.setOwnerPartyId(PARTY_ID);

        account2 = new PSDAccountDTO();
        account2.setResourceId(UUID.fromString("550e8400-e29b-41d4-a716-446655440014"));
        account2.setIban("DE89370400440532013001");
        account2.setOwnerPartyId(PARTY_ID);

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
    void getAccounts_shouldReturnAccounts_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "ACCOUNT", "READ")).thenReturn(Mono.just(true));
        when(accountServicePort.getAccountsByPartyId(PARTY_ID)).thenReturn(Flux.fromIterable(Arrays.asList(account1, account2)));

        // When & Then
        StepVerifier.create(accountInformationService.getAccounts(CONSENT_ID, PARTY_ID))
                .expectNext(account1)
                .expectNext(account2)
                .verifyComplete();
    }

    @Test
    void getAccounts_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "ACCOUNT", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountInformationService.getAccounts(CONSENT_ID, PARTY_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getAccount_shouldReturnAccount_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "ACCOUNT", "READ")).thenReturn(Mono.just(true));
        when(accountServicePort.getAccountById(ACCOUNT_ID)).thenReturn(Mono.just(account1));

        // When & Then
        StepVerifier.create(accountInformationService.getAccount(CONSENT_ID, ACCOUNT_ID))
                .expectNext(account1)
                .verifyComplete();
    }

    @Test
    void getAccount_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "ACCOUNT", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountInformationService.getAccount(CONSENT_ID, ACCOUNT_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getBalances_shouldReturnBalances_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "BALANCE", "READ")).thenReturn(Mono.just(true));
        when(accountServicePort.getBalancesByAccountId(ACCOUNT_ID)).thenReturn(Flux.fromIterable(Arrays.asList(balance1, balance2)));

        // When & Then
        StepVerifier.create(accountInformationService.getBalances(CONSENT_ID, ACCOUNT_ID))
                .expectNext(balance1)
                .expectNext(balance2)
                .verifyComplete();
    }

    @Test
    void getBalances_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "BALANCE", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountInformationService.getBalances(CONSENT_ID, ACCOUNT_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getTransactions_shouldReturnTransactions_whenConsentIsValid() {
        // Given
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        when(consentService.validateConsent(CONSENT_ID, "TRANSACTION", "READ")).thenReturn(Mono.just(true));
        when(transactionServicePort.getTransactionsByAccountId(ACCOUNT_ID, fromDate, toDate))
                .thenReturn(Flux.fromIterable(Arrays.asList(transaction1, transaction2)));

        // When & Then
        StepVerifier.create(accountInformationService.getTransactions(CONSENT_ID, ACCOUNT_ID, fromDate, toDate))
                .expectNext(transaction1)
                .expectNext(transaction2)
                .verifyComplete();
    }

    @Test
    void getTransactions_shouldReturnError_whenConsentIsInvalid() {
        // Given
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        when(consentService.validateConsent(CONSENT_ID, "TRANSACTION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountInformationService.getTransactions(CONSENT_ID, ACCOUNT_ID, fromDate, toDate))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getTransaction_shouldReturnTransaction_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "TRANSACTION", "READ")).thenReturn(Mono.just(true));
        when(transactionServicePort.getTransactionById(TRANSACTION_ID)).thenReturn(Mono.just(transaction1));

        // When & Then
        StepVerifier.create(accountInformationService.getTransaction(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID))
                .expectNext(transaction1)
                .verifyComplete();
    }

    @Test
    void getTransaction_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "TRANSACTION", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountInformationService.getTransaction(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
