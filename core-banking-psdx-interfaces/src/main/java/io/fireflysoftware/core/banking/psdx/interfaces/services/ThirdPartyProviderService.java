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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderRegistrationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for managing Third Party Providers (TPPs) according to PSD2/PSD3 regulations.
 */
public interface ThirdPartyProviderService {

    /**
     * Register a new Third Party Provider.
     *
     * @param registration The registration details
     * @return A Mono of the registered TPP
     */
    Mono<PSDThirdPartyProviderDTO> registerProvider(PSDThirdPartyProviderRegistrationDTO registration);

    /**
     * Get a Third Party Provider by its ID.
     *
     * @param providerId The ID of the TPP
     * @return A Mono of the TPP
     */
    Mono<PSDThirdPartyProviderDTO> getProvider(UUID providerId);

    /**
     * Get all Third Party Providers.
     *
     * @return A Flux of TPPs
     */
    Flux<PSDThirdPartyProviderDTO> getAllProviders();

    /**
     * Update a Third Party Provider.
     *
     * @param providerId The ID of the TPP
     * @param providerUpdate The updated details
     * @return A Mono of the updated TPP
     */
    Mono<PSDThirdPartyProviderDTO> updateProvider(UUID providerId, PSDThirdPartyProviderDTO providerUpdate);

    /**
     * Suspend a Third Party Provider.
     *
     * @param providerId The ID of the TPP
     * @return A Mono of the suspended TPP
     */
    Mono<PSDThirdPartyProviderDTO> suspendProvider(UUID providerId);

    /**
     * Activate a Third Party Provider.
     *
     * @param providerId The ID of the TPP
     * @return A Mono of the activated TPP
     */
    Mono<PSDThirdPartyProviderDTO> activateProvider(UUID providerId);

    /**
     * Revoke a Third Party Provider.
     *
     * @param providerId The ID of the TPP
     * @return A Mono of Boolean indicating if the TPP was revoked
     */
    Mono<Boolean> revokeProvider(UUID providerId);

    /**
     * Validate a Third Party Provider's API key.
     *
     * @param apiKey The API key of the TPP
     * @return A Mono of the TPP
     */
    Mono<PSDThirdPartyProviderDTO> validateApiKey(String apiKey);
}
