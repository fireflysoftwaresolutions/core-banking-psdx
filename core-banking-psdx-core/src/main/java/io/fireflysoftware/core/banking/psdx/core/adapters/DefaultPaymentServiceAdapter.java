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

import io.fireflysoftware.core.banking.psdx.core.ports.PaymentServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Default implementation of the PaymentServicePort.
 * This implementation throws exceptions as "not yet implemented".
 */
@Component
public class DefaultPaymentServiceAdapter implements PaymentServicePort {

    @Override
    public Mono<PSDPaymentDTO> initiatePayment(PSDPaymentInitiationRequestDTO paymentRequest) {
        return Mono.error(new UnsupportedOperationException("Method initiatePayment not yet implemented"));
    }

    @Override
    public Mono<PSDPaymentStatusDTO> getPaymentStatus(UUID paymentId) {
        return Mono.error(new UnsupportedOperationException("Method getPaymentStatus not yet implemented"));
    }

    @Override
    public Mono<PSDPaymentDTO> getPayment(UUID paymentId) {
        return Mono.error(new UnsupportedOperationException("Method getPayment not yet implemented"));
    }

    @Override
    public Mono<Boolean> cancelPayment(UUID paymentId) {
        return Mono.error(new UnsupportedOperationException("Method cancelPayment not yet implemented"));
    }

    @Override
    public Mono<PSDPaymentDTO> authorizePayment(UUID paymentId, String authorizationCode) {
        return Mono.error(new UnsupportedOperationException("Method authorizePayment not yet implemented"));
    }
}
