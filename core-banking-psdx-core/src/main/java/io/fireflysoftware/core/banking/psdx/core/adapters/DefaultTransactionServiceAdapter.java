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

import io.fireflysoftware.core.banking.psdx.core.ports.TransactionServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * In-memory booked transaction. The same account or transaction id always returns the same record.
 */
@Component
public class DefaultTransactionServiceAdapter implements TransactionServicePort {

    @Override
    public Flux<PSDTransactionDTO> getTransactionsByAccountId(UUID accountId, LocalDate fromDate, LocalDate toDate) {
        UUID transactionId = SandboxSamples.id("transaction", SandboxSamples.text(accountId));
        return Flux.just(transaction(transactionId, fromDate));
    }

    @Override
    public Mono<PSDTransactionDTO> getTransactionById(UUID transactionId) {
        return Mono.just(transaction(transactionId, null));
    }

    static PSDTransactionDTO transaction(UUID transactionId, LocalDate bookingDate) {
        LocalDate bookedOn = bookingDate != null ? bookingDate : SandboxSamples.SAMPLE_DATE;
        return PSDTransactionDTO.builder()
                .transactionId(transactionId)
                .transactionStatus("booked")
                .bookingDate(bookedOn)
                .valueDate(bookedOn)
                .creditorName("Sandbox merchant")
                .transactionAmount(PSDTransactionDTO.PSDAmountDTO.builder()
                        .currency(SandboxSamples.SAMPLE_CURRENCY)
                        .amount(new BigDecimal("42.50"))
                        .build())
                .build();
    }
}
