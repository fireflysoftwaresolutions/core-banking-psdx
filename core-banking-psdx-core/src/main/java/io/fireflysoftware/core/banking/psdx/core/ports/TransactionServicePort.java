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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Port for interacting with the transaction service.
 */
public interface TransactionServicePort {

    /**
     * Get transactions for a specific account within a date range.
     *
     * @param accountId The ID of the account
     * @param fromDate The start date of the range
     * @param toDate The end date of the range
     * @return A Flux of transactions
     */
    Flux<PSDTransactionDTO> getTransactionsByAccountId(UUID accountId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get a specific transaction.
     *
     * @param transactionId The ID of the transaction
     * @return A Mono of the transaction
     */
    Mono<PSDTransactionDTO> getTransactionById(UUID transactionId);
}
