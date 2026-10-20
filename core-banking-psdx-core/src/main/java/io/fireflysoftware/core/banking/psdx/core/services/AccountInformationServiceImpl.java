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

import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.banking.psdx.core.ports.TransactionServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccountInformationService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementation of the AccountInformationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountInformationServiceImpl implements AccountInformationService {

    private final AccountServicePort accountServicePort;
    private final TransactionServicePort transactionServicePort;
    private final ConsentService consentService;

    @Override
    public Flux<PSDAccountDTO> getAccounts(UUID consentId, UUID partyId) {
        log.debug("Getting accounts for party ID: {} with consent ID: {}", partyId, consentId);

        return consentService.validateConsent(consentId, "ACCOUNT", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return accountServicePort.getAccountsByPartyId(partyId)
                            .doOnComplete(() -> log.debug("Retrieved accounts for party ID: {}", partyId));
                });
    }

    @Override
    public Mono<PSDAccountDTO> getAccount(UUID consentId, UUID accountId) {
        log.debug("Getting account with ID: {} using consent ID: {}", accountId, consentId);

        return consentService.validateConsent(consentId, "ACCOUNT", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return accountServicePort.getAccountById(accountId)
                            .doOnSuccess(account -> log.debug("Retrieved account with ID: {}", accountId));
                });
    }

    @Override
    public Flux<PSDBalanceDTO> getBalances(UUID consentId, UUID accountId) {
        log.debug("Getting balances for account ID: {} using consent ID: {}", accountId, consentId);

        return consentService.validateConsent(consentId, "BALANCE", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return accountServicePort.getBalancesByAccountId(accountId)
                            .doOnComplete(() -> log.debug("Retrieved balances for account ID: {}", accountId));
                });
    }

    @Override
    public Flux<PSDTransactionDTO> getTransactions(UUID consentId, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        log.debug("Getting transactions for account ID: {} between {} and {} using consent ID: {}",
                accountId, fromDate, toDate, consentId);

        return consentService.validateConsent(consentId, "TRANSACTION", "READ")
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        return Flux.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return transactionServicePort.getTransactionsByAccountId(accountId, fromDate, toDate)
                            .doOnComplete(() -> log.debug("Retrieved transactions for account ID: {} in date range", accountId));
                });
    }

    @Override
    public Mono<PSDTransactionDTO> getTransaction(UUID consentId, UUID accountId, UUID transactionId) {
        log.debug("Getting transaction with ID: {} for account ID: {} using consent ID: {}",
                transactionId, accountId, consentId);

        return consentService.validateConsent(consentId, "TRANSACTION", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return transactionServicePort.getTransactionById(transactionId)
                            .doOnSuccess(transaction -> log.debug("Retrieved transaction with ID: {}", transactionId));
                });
    }
}
