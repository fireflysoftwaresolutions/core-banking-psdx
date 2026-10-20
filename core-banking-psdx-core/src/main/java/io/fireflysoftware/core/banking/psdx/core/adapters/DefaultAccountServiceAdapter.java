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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Default implementation of the AccountServicePort.
 * This implementation throws exceptions as "not yet implemented".
 */
@Component
public class DefaultAccountServiceAdapter implements AccountServicePort {

    @Override
    public Flux<PSDAccountDTO> getAccountsByPartyId(UUID partyId) {
        return Flux.error(new UnsupportedOperationException("Method getAccountsByPartyId not yet implemented"));
    }

    @Override
    public Mono<PSDAccountDTO> getAccountById(UUID accountId) {
        return Mono.error(new UnsupportedOperationException("Method getAccountById not yet implemented"));
    }

    @Override
    public Flux<PSDBalanceDTO> getBalancesByAccountId(UUID accountId) {
        return Flux.error(new UnsupportedOperationException("Method getBalancesByAccountId not yet implemented"));
    }
}
