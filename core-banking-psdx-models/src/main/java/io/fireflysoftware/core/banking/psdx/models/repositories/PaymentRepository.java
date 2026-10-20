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


package io.fireflysoftware.core.banking.psdx.models.repositories;

import io.fireflysoftware.core.banking.psdx.models.entities.Payment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for managing Payment entities.
 */
@Repository
public interface PaymentRepository extends ReactiveCrudRepository<Payment, UUID> {

    /**
     * Find all payments for a specific consent.
     *
     * @param consentId The ID of the consent
     * @return A Flux of payments
     */
    Flux<Payment> findByConsentId(UUID consentId);

    /**
     * Find a payment by its end-to-end identification.
     *
     * @param endToEndIdentification The end-to-end identification
     * @return A Mono of the payment
     */
    Mono<Payment> findByEndToEndIdentification(String endToEndIdentification);

    /**
     * Find all payments with a specific transaction status.
     *
     * @param transactionStatus The transaction status
     * @return A Flux of payments
     */
    Flux<Payment> findByTransactionStatus(String transactionStatus);

    /**
     * Find all payments for a specific debtor account.
     *
     * @param debtorAccount The debtor account
     * @return A Flux of payments
     */
    Flux<Payment> findByDebtorAccount(String debtorAccount);

    /**
     * Find all payments for a specific creditor account.
     *
     * @param creditorAccount The creditor account
     * @return A Flux of payments
     */
    Flux<Payment> findByCreditorAccount(String creditorAccount);

    /**
     * Find all payments with a specific requested execution date.
     *
     * @param requestedExecutionDate The requested execution date
     * @return A Flux of payments
     */
    Flux<Payment> findByRequestedExecutionDate(LocalDate requestedExecutionDate);

    /**
     * Find all payments created within a date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return A Flux of payments
     */
    @Query("SELECT * FROM payments WHERE created_at BETWEEN :startDate AND :endDate")
    Flux<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all payments for a specific payment type.
     *
     * @param paymentType The payment type
     * @return A Flux of payments
     */
    Flux<Payment> findByPaymentType(String paymentType);

    /**
     * Find all payments for a specific debtor account and transaction status.
     *
     * @param debtorAccount The debtor account
     * @param transactionStatus The transaction status
     * @return A Flux of payments
     */
    Flux<Payment> findByDebtorAccountAndTransactionStatus(String debtorAccount, String transactionStatus);

    /**
     * Find all payments for a specific creditor account and transaction status.
     *
     * @param creditorAccount The creditor account
     * @param transactionStatus The transaction status
     * @return A Flux of payments
     */
    Flux<Payment> findByCreditorAccountAndTransactionStatus(String creditorAccount, String transactionStatus);
}