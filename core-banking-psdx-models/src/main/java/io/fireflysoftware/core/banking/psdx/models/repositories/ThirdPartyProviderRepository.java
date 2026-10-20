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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderType;
import io.fireflysoftware.core.banking.psdx.models.entities.ThirdPartyProvider;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository for managing ThirdPartyProvider entities.
 */
@Repository
public interface ThirdPartyProviderRepository extends ReactiveCrudRepository<ThirdPartyProvider, UUID> {

    /**
     * Find a third party provider by its API key.
     *
     * @param apiKey The API key of the third party provider
     * @return A Mono of the third party provider
     */
    Mono<ThirdPartyProvider> findByApiKey(String apiKey);

    /**
     * Find a third party provider by its registration number.
     *
     * @param registrationNumber The registration number of the third party provider
     * @return A Mono of the third party provider
     */
    Mono<ThirdPartyProvider> findByRegistrationNumber(String registrationNumber);

    /**
     * Find all third party providers of a specific type.
     *
     * @param providerType The type of the third party provider
     * @return A Flux of third party providers
     */
    Flux<ThirdPartyProvider> findByProviderType(ProviderType providerType);

    /**
     * Find all active third party providers.
     *
     * @param status The status of the third party provider
     * @return A Flux of third party providers
     */
    Flux<ThirdPartyProvider> findByStatus(ProviderStatus status);
}
