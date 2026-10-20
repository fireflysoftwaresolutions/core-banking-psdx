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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for Funds Confirmation Services (FCS) according to PSD2/PSD3 regulations.
 */
public interface FundsConfirmationService {

    /**
     * Confirm the availability of funds for a specific amount.
     *
     * @param consentId The ID of the consent
     * @param fundsConfirmationRequest The funds confirmation request
     * @return A Mono of the funds confirmation response
     */
    Mono<PSDFundsConfirmationDTO> confirmFunds(UUID consentId, PSDFundsConfirmationDTO fundsConfirmationRequest);

    /**
     * Get a funds confirmation by its ID.
     *
     * @param consentId The ID of the consent
     * @param fundsConfirmationId The ID of the funds confirmation
     * @return A Mono of the funds confirmation
     */
    Mono<PSDFundsConfirmationDTO> getFundsConfirmation(UUID consentId, UUID fundsConfirmationId);
}
