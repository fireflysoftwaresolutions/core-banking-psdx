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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationResponseDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Port for the SCA (Strong Customer Authentication) service.
 */
public interface SCAServicePort {

    /**
     * Initiate SCA for a customer.
     *
     * @param request The SCA authentication request
     * @return A Mono of PSDSCAAuthenticationResponseDTO
     */
    Mono<PSDSCAAuthenticationResponseDTO> initiateSCA(PSDSCAAuthenticationRequestDTO request);

    /**
     * Validate SCA for a customer.
     *
     * @param request The SCA validation request
     * @return A Mono of PSDSCAValidationResponseDTO
     */
    Mono<PSDSCAValidationResponseDTO> validateSCA(PSDSCAValidationRequestDTO request);

    /**
     * Check if SCA is required for a payment.
     *
     * @param amount The payment amount
     * @param currency The payment currency
     * @return A Mono of Boolean indicating if SCA is required
     */
    Mono<Boolean> isSCARequired(Double amount, String currency);
}
