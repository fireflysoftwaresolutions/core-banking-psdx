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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port for interacting with the account service.
 */
public interface AccountServicePort {

    /**
     * Get all accounts for a customer.
     *
     * @param partyId The ID of the customer
     * @return A Flux of accounts
     */
    Flux<PSDAccountDTO> getAccountsByPartyId(UUID partyId);

    /**
     * Get a specific account.
     *
     * @param accountId The ID of the account
     * @return A Mono of the account
     */
    Mono<PSDAccountDTO> getAccountById(UUID accountId);

    /**
     * Get balances for a specific account.
     *
     * @param accountId The ID of the account
     * @return A Flux of balances
     */
    Flux<PSDBalanceDTO> getBalancesByAccountId(UUID accountId);
}
