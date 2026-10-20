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

import io.fireflysoftware.core.banking.psdx.core.ports.PaymentServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.PaymentInitiationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of the PaymentInitiationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiationServiceImpl implements PaymentInitiationService {

    private final PaymentServicePort paymentServicePort;
    private final ConsentService consentService;

    @Override
    public Mono<PSDPaymentDTO> initiatePayment(UUID consentId, PSDPaymentInitiationRequestDTO paymentRequest) {
        log.debug("Initiating payment using consent ID: {}", consentId);

        return consentService.validateConsent(consentId, "PAYMENT", "WRITE")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return paymentServicePort.initiatePayment(paymentRequest)
                            .doOnSuccess(payment -> log.info("Payment initiated with ID: {}", payment.getPaymentId()));
                });
    }

    @Override
    public Mono<PSDPaymentStatusDTO> getPaymentStatus(UUID consentId, UUID paymentId) {
        log.debug("Getting status for payment ID: {} using consent ID: {}", paymentId, consentId);

        return consentService.validateConsent(consentId, "PAYMENT", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return paymentServicePort.getPaymentStatus(paymentId)
                            .doOnSuccess(status -> log.debug("Retrieved status for payment ID: {}: {}",
                                    paymentId, status.getStatus()));
                });
    }

    @Override
    public Mono<PSDPaymentDTO> getPayment(UUID consentId, UUID paymentId) {
        log.debug("Getting payment with ID: {} using consent ID: {}", paymentId, consentId);

        return consentService.validateConsent(consentId, "PAYMENT", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return paymentServicePort.getPayment(paymentId)
                            .doOnSuccess(payment -> log.debug("Retrieved payment with ID: {}", paymentId));
                });
    }

    @Override
    public Mono<PSDPaymentDTO> cancelPayment(UUID consentId, UUID paymentId) {
        log.debug("Cancelling payment with ID: {} using consent ID: {}", paymentId, consentId);

        return consentService.validateConsent(consentId, "PAYMENT", "WRITE")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return paymentServicePort.cancelPayment(paymentId)
                            .flatMap(result -> {
                                if (result) {
                                    log.info("Payment with ID: {} cancelled successfully", paymentId);
                                    // Get the updated payment after cancellation
                                    return paymentServicePort.getPayment(paymentId)
                                            .map(payment -> {
                                                // Update the status to CANC
                                                payment.setTransactionStatus("CANC");
                                                return payment;
                                            });
                                } else {
                                    log.warn("Failed to cancel payment with ID: {}", paymentId);
                                    return Mono.error(new IllegalStateException("Failed to cancel payment"));
                                }
                            });
                });
    }

    @Override
    public Mono<PSDPaymentDTO> authorizePayment(UUID consentId, UUID paymentId, String authorizationCode) {
        log.debug("Authorizing payment with ID: {} using consent ID: {}", paymentId, consentId);

        return consentService.validateConsent(consentId, "PAYMENT", "WRITE")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    return paymentServicePort.authorizePayment(paymentId, authorizationCode)
                            .doOnSuccess(payment -> log.info("Payment with ID: {} authorized successfully", paymentId));
                });
    }
}
