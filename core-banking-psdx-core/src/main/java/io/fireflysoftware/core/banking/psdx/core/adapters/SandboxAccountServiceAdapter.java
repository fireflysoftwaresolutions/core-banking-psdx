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


package io.fireflysoftware.core.banking.psdx.core.adapters;

import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * In-memory account sample. The same party or account id always returns the same record.
 */
public class SandboxAccountServiceAdapter implements AccountServicePort {

    @Override
    public Flux<PSDAccountDTO> getAccountsByPartyId(UUID partyId) {
        UUID accountId = SandboxSamples.id("account", SandboxSamples.text(partyId));
        return Flux.just(account(accountId, partyId));
    }

    @Override
    public Mono<PSDAccountDTO> getAccountById(UUID accountId) {
        UUID ownerId = SandboxSamples.id("owner", SandboxSamples.text(accountId));
        return Mono.just(account(accountId, ownerId));
    }

    @Override
    public Flux<PSDBalanceDTO> getBalancesByAccountId(UUID accountId) {
        return Flux.just(balance(new BigDecimal("125.00")));
    }

    @Override
    public Flux<PSDBalanceDTO> balancesForReference(String accountReference) {
        if (accountReference == null) {
            return Flux.empty();
        }
        String compact = accountReference.replace(" ", "");
        if (!SandboxSamples.SAMPLE_IBAN.equals(compact)) {
            return Flux.empty();
        }
        return Flux.just(balance(new BigDecimal("125.00")));
    }

    private static PSDAccountDTO account(UUID accountId, UUID ownerId) {
        return PSDAccountDTO.builder()
                .resourceId(accountId)
                .ownerPartyId(ownerId)
                .iban(SandboxSamples.SAMPLE_IBAN)
                .currency(SandboxSamples.SAMPLE_CURRENCY)
                .name("Sandbox account")
                .product("Sandbox current account")
                .cashAccountType("CACC")
                .status("enabled")
                .bic("DEUTDEFF")
                .createdAt(SandboxSamples.SAMPLE_TIME)
                .updatedAt(SandboxSamples.SAMPLE_TIME)
                .build();
    }

    public static PSDBalanceDTO balance(BigDecimal amount) {
        return PSDBalanceDTO.builder()
                .balanceType("closingBooked")
                .creditDebitIndicator("CRDT")
                .referenceDate(SandboxSamples.SAMPLE_TIME)
                .balanceAmount(PSDBalanceDTO.PSDAmountDTO.builder()
                        .currency(SandboxSamples.SAMPLE_CURRENCY)
                        .amount(amount)
                        .build())
                .build();
    }
}
