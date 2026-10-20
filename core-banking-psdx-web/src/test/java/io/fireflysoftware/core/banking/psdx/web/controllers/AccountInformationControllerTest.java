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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccountInformationService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountInformationControllerTest {

    @Mock
    private AccountInformationService accountInformationService;

    @InjectMocks
    private AccountInformationController accountInformationController;

    private WebTestClient webTestClient;

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
        webTestClient = WebTestClient.bindToController(accountInformationController).build();

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
    void getAccounts_shouldReturnAccounts() {
        // Given
        when(accountInformationService.getAccounts(CONSENT_ID, PARTY_ID))
                .thenReturn(Flux.fromIterable(Arrays.asList(account1, account2)));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/accounts").queryParam("partyId", PARTY_ID).build())
                .header("X-Consent-ID", CONSENT_ID.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PSDAccountDTO.class)
                .hasSize(2)
                .contains(account1, account2);
    }

    @Test
    void getAccount_shouldReturnAccount() {
        // Given
        when(accountInformationService.getAccount(CONSENT_ID, ACCOUNT_ID))
                .thenReturn(Mono.just(account1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/accounts/{accountId}", ACCOUNT_ID)
                .header("X-Consent-ID", CONSENT_ID.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDAccountDTO.class)
                .isEqualTo(account1);
    }

    @Test
    void getBalances_shouldReturnBalances() {
        // Given
        when(accountInformationService.getBalances(CONSENT_ID, ACCOUNT_ID))
                .thenReturn(Flux.fromIterable(Arrays.asList(balance1, balance2)));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/accounts/{accountId}/balances", ACCOUNT_ID)
                .header("X-Consent-ID", CONSENT_ID.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PSDBalanceDTO.class)
                .hasSize(2)
                .contains(balance1, balance2);
    }

    @Test
    void getTransactions_shouldReturnTransactions() {
        // Given
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        when(accountInformationService.getTransactions(any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Flux.fromIterable(Arrays.asList(transaction1, transaction2)));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/accounts/{accountId}/transactions")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", toDate)
                        .build(ACCOUNT_ID))
                .header("X-Consent-ID", CONSENT_ID.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PSDTransactionDTO.class)
                .hasSize(2)
                .contains(transaction1, transaction2);
    }

    @Test
    void getTransaction_shouldReturnTransaction() {
        // Given
        when(accountInformationService.getTransaction(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID))
                .thenReturn(Mono.just(transaction1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/accounts/{accountId}/transactions/{transactionId}", ACCOUNT_ID, TRANSACTION_ID)
                .header("X-Consent-ID", CONSENT_ID.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDTransactionDTO.class)
                .isEqualTo(transaction1);
    }
}
