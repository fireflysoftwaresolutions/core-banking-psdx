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

import io.fireflysoftware.core.banking.psdx.core.ports.CardServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDCardAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * In-memory card sample. The same party, card, or transaction id always returns the same record.
 */
@Component
public class DefaultCardServiceAdapter implements CardServicePort {

    @Override
    public Flux<PSDCardAccountDTO> getCardAccountsByPartyId(UUID partyId) {
        UUID cardId = SandboxSamples.id("card", SandboxSamples.text(partyId));
        return Flux.just(card(cardId, partyId));
    }

    @Override
    public Mono<PSDCardAccountDTO> getCardAccountById(UUID cardId) {
        UUID ownerId = SandboxSamples.id("card-owner", SandboxSamples.text(cardId));
        return Mono.just(card(cardId, ownerId));
    }

    @Override
    public Flux<PSDBalanceDTO> getBalancesByCardId(UUID cardId) {
        return Flux.just(DefaultAccountServiceAdapter.balance(new BigDecimal("250.00")));
    }

    @Override
    public Flux<PSDTransactionDTO> getTransactionsByCardId(UUID cardId, LocalDate fromDate, LocalDate toDate) {
        UUID transactionId = SandboxSamples.id("card-transaction", SandboxSamples.text(cardId));
        return Flux.just(DefaultTransactionServiceAdapter.transaction(transactionId, fromDate));
    }

    @Override
    public Mono<PSDTransactionDTO> getTransactionByCardIdAndTransactionId(UUID cardId, UUID transactionId) {
        return Mono.just(DefaultTransactionServiceAdapter.transaction(transactionId, null));
    }

    private static PSDCardAccountDTO card(UUID cardId, UUID ownerId) {
        return PSDCardAccountDTO.builder()
                .resourceId(cardId)
                .ownerPartyId(ownerId)
                .maskedPan("540905******0000")
                .currency(SandboxSamples.SAMPLE_CURRENCY)
                .name("Sandbox card")
                .product("Sandbox debit card")
                .status("enabled")
                .creditLimit("500.00")
                .cardType("debit")
                .cardNetwork("visa")
                .createdAt(SandboxSamples.SAMPLE_TIME)
                .updatedAt(SandboxSamples.SAMPLE_TIME)
                .build();
    }
}
