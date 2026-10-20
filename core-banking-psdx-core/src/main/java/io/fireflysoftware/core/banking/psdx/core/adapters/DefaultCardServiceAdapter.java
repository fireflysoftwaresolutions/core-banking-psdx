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

import java.time.LocalDate;
import java.util.UUID;

/**
 * Default implementation of the CardServicePort.
 * This implementation throws exceptions as "not yet implemented".
 */
@Component
public class DefaultCardServiceAdapter implements CardServicePort {

    @Override
    public Flux<PSDCardAccountDTO> getCardAccountsByPartyId(UUID partyId) {
        return Flux.error(new UnsupportedOperationException("Method getCardAccountsByPartyId not yet implemented"));
    }

    @Override
    public Mono<PSDCardAccountDTO> getCardAccountById(UUID cardId) {
        return Mono.error(new UnsupportedOperationException("Method getCardAccountById not yet implemented"));
    }

    @Override
    public Flux<PSDBalanceDTO> getBalancesByCardId(UUID cardId) {
        return Flux.error(new UnsupportedOperationException("Method getBalancesByCardId not yet implemented"));
    }

    @Override
    public Flux<PSDTransactionDTO> getTransactionsByCardId(UUID cardId, LocalDate fromDate, LocalDate toDate) {
        return Flux.error(new UnsupportedOperationException("Method getTransactionsByCardId not yet implemented"));
    }

    @Override
    public Mono<PSDTransactionDTO> getTransactionByCardIdAndTransactionId(UUID cardId, UUID transactionId) {
        return Mono.error(new UnsupportedOperationException("Method getTransactionByCardIdAndTransactionId not yet implemented"));
    }
}
