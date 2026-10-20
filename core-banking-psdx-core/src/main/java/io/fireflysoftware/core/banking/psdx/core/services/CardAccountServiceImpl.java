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

import io.fireflysoftware.core.banking.psdx.core.ports.CardServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDCardAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.CardAccountService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementation of the CardAccountService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardAccountServiceImpl implements CardAccountService {

    private final CardServicePort cardServicePort;
    private final ConsentService consentService;

    @Override
    public Flux<PSDCardAccountDTO> getCardAccounts(UUID consentId, UUID partyId) {
        log.debug("Getting card accounts for party ID: {} with consent ID: {}", partyId, consentId);

        return consentService.validateConsent(consentId, "CARD", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return cardServicePort.getCardAccountsByPartyId(partyId)
                            .doOnComplete(() -> log.debug("Retrieved card accounts for party ID: {}", partyId));
                });
    }

    @Override
    public Mono<PSDCardAccountDTO> getCardAccount(UUID consentId, UUID cardId) {
        log.debug("Getting card account with ID: {} using consent ID: {}", cardId, consentId);

        return consentService.validateConsent(consentId, "CARD", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return cardServicePort.getCardAccountById(cardId)
                            .doOnSuccess(account -> log.debug("Retrieved card account with ID: {}", cardId));
                });
    }

    @Override
    public Flux<PSDBalanceDTO> getCardBalances(UUID consentId, UUID cardId) {
        log.debug("Getting balances for card ID: {} using consent ID: {}", cardId, consentId);

        return consentService.validateConsent(consentId, "CARD_BALANCE", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return cardServicePort.getBalancesByCardId(cardId)
                            .doOnComplete(() -> log.debug("Retrieved balances for card ID: {}", cardId));
                });
    }

    @Override
    public Flux<PSDTransactionDTO> getCardTransactions(UUID consentId, UUID cardId, LocalDate fromDate, LocalDate toDate) {
        log.debug("Getting transactions for card ID: {} between {} and {} using consent ID: {}",
                cardId, fromDate, toDate, consentId);

        return consentService.validateConsent(consentId, "CARD_TRANSACTION", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return cardServicePort.getTransactionsByCardId(cardId, fromDate, toDate)
                            .doOnComplete(() -> log.debug("Retrieved transactions for card ID: {} in date range", cardId));
                });
    }

    @Override
    public Mono<PSDTransactionDTO> getCardTransaction(UUID consentId, UUID cardId, UUID transactionId) {
        log.debug("Getting transaction with ID: {} for card ID: {} using consent ID: {}",
                transactionId, cardId, consentId);

        return consentService.validateConsent(consentId, "CARD_TRANSACTION", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return cardServicePort.getTransactionByCardIdAndTransactionId(cardId, transactionId)
                            .doOnSuccess(transaction -> log.debug("Retrieved transaction with ID: {}", transactionId));
                });
    }
}
