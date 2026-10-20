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


package io.fireflysoftware.core.banking.psdx.interfaces.services;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for Payment Initiation Services (PIS) according to PSD2/PSD3 regulations.
 */
public interface PaymentInitiationService {

    /**
     * Initiate a new payment.
     *
     * @param consentId The ID of the consent
     * @param paymentRequest The payment initiation request
     * @return A Mono of the initiated payment
     */
    Mono<PSDPaymentDTO> initiatePayment(UUID consentId, PSDPaymentInitiationRequestDTO paymentRequest);

    /**
     * Get the status of a payment.
     *
     * @param consentId The ID of the consent
     * @param paymentId The ID of the payment
     * @return A Mono of the payment status
     */
    Mono<PSDPaymentStatusDTO> getPaymentStatus(UUID consentId, UUID paymentId);

    /**
     * Get the details of a payment.
     *
     * @param consentId The ID of the consent
     * @param paymentId The ID of the payment
     * @return A Mono of the payment
     */
    Mono<PSDPaymentDTO> getPayment(UUID consentId, UUID paymentId);

    /**
     * Cancel a payment.
     *
     * @param consentId The ID of the consent
     * @param paymentId The ID of the payment
     * @return A Mono of the cancelled payment
     */
    Mono<PSDPaymentDTO> cancelPayment(UUID consentId, UUID paymentId);

    /**
     * Authorize a payment using Strong Customer Authentication (SCA).
     *
     * @param consentId The ID of the consent
     * @param paymentId The ID of the payment
     * @param authorizationCode The authorization code from SCA
     * @return A Mono of the payment
     */
    Mono<PSDPaymentDTO> authorizePayment(UUID consentId, UUID paymentId, String authorizationCode);
}
