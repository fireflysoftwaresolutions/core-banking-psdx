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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentStatusDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for managing consents according to PSD2/PSD3 regulations.
 */
public interface ConsentService {

    /**
     * Create a new consent for a customer.
     *
     * @param consentRequest The consent request
     * @return A Mono of the created consent
     */
    Mono<PSDConsentDTO> createConsent(PSDConsentRequestDTO consentRequest);

    /**
     * Get a consent by its ID.
     *
     * @param consentId The ID of the consent
     * @return A Mono of the consent
     */
    Mono<PSDConsentDTO> getConsent(UUID consentId);

    /**
     * Get all consents for a customer.
     *
     * @param partyId The ID of the customer
     * @return A Flux of consents
     */
    Flux<PSDConsentDTO> getConsentsForCustomer(UUID partyId);

    /**
     * Update the status of a consent.
     *
     * @param consentId The ID of the consent
     * @param statusUpdate The new status
     * @return A Mono of the updated consent
     */
    Mono<PSDConsentDTO> updateConsentStatus(UUID consentId, PSDConsentStatusDTO statusUpdate);

    /**
     * Revoke a consent.
     *
     * @param consentId The ID of the consent
     * @return A Mono of the revoked consent
     */
    Mono<PSDConsentDTO> revokeConsent(UUID consentId);

    /**
     * Validate a consent for a specific operation.
     *
     * @param consentId The ID of the consent
     * @param resourceType The type of resource being accessed
     * @param accessType The type of access
     * @return A Mono of Boolean indicating if the consent is valid
     */
    Mono<Boolean> validateConsent(UUID consentId, String resourceType, String accessType);

    /**
     * Get the status of a consent.
     *
     * @param consentId The ID of the consent
     * @return A Mono of the consent status
     */
    Mono<PSDConsentStatusDTO> getConsentStatus(UUID consentId);
}
