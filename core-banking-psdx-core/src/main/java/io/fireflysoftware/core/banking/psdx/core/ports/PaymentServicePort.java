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


package io.fireflysoftware.core.banking.psdx.core.ports;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port for interacting with the payment service.
 */
public interface PaymentServicePort {

    /**
     * Initiate a new payment.
     *
     * @param paymentRequest The payment initiation request
     * @return A Mono of the initiated payment
     */
    Mono<PSDPaymentDTO> initiatePayment(PSDPaymentInitiationRequestDTO paymentRequest);

    /**
     * Get the status of a payment.
     *
     * @param paymentId The ID of the payment
     * @return A Mono of the payment status
     */
    Mono<PSDPaymentStatusDTO> getPaymentStatus(UUID paymentId);

    /**
     * Get the details of a payment.
     *
     * @param paymentId The ID of the payment
     * @return A Mono of the payment
     */
    Mono<PSDPaymentDTO> getPayment(UUID paymentId);

    /**
     * Cancel a payment.
     *
     * @param paymentId The ID of the payment
     * @return A Mono of Boolean indicating if the payment was cancelled
     */
    Mono<Boolean> cancelPayment(UUID paymentId);

    /**
     * Authorize a payment.
     *
     * @param paymentId The ID of the payment
     * @param authorizationCode The authorization code
     * @return A Mono of the payment
     */
    Mono<PSDPaymentDTO> authorizePayment(UUID paymentId, String authorizationCode);
}
