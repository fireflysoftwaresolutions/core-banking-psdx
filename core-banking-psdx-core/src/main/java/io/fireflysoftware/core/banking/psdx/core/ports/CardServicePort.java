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


package io.fireflysoftware.core.banking.psdx.core.ports;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDCardAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Port for interacting with the card service.
 */
public interface CardServicePort {

    /**
     * Get all card accounts for a customer.
     *
     * @param partyId The ID of the customer
     * @return A Flux of card accounts
     */
    Flux<PSDCardAccountDTO> getCardAccountsByPartyId(UUID partyId);

    /**
     * Get a specific card account.
     *
     * @param cardId The ID of the card
     * @return A Mono of the card account
     */
    Mono<PSDCardAccountDTO> getCardAccountById(UUID cardId);

    /**
     * Get balances for a specific card account.
     *
     * @param cardId The ID of the card
     * @return A Flux of balances
     */
    Flux<PSDBalanceDTO> getBalancesByCardId(UUID cardId);

    /**
     * Get transactions for a specific card account within a date range.
     *
     * @param cardId The ID of the card
     * @param fromDate The start date of the range
     * @param toDate The end date of the range
     * @return A Flux of transactions
     */
    Flux<PSDTransactionDTO> getTransactionsByCardId(UUID cardId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get a specific transaction for a card account.
     *
     * @param cardId The ID of the card
     * @param transactionId The ID of the transaction
     * @return A Mono of the transaction
     */
    Mono<PSDTransactionDTO> getTransactionByCardIdAndTransactionId(UUID cardId, UUID transactionId);
}
