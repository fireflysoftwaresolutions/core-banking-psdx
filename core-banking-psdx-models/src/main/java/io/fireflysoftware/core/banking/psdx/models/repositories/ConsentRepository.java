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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
import io.fireflysoftware.core.banking.psdx.models.entities.Consent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for managing Consent entities.
 */
@Repository
public interface ConsentRepository extends ReactiveCrudRepository<Consent, UUID> {

    /**
     * Find all consents for a specific party.
     *
     * @param partyId The ID of the party
     * @return A Flux of consents
     */
    Flux<Consent> findByPartyId(UUID partyId);

    /**
     * Find all valid consents for a specific party.
     *
     * @param partyId The ID of the party
     * @param status The status of the consent
     * @param now The current date and time
     * @return A Flux of consents
     */
    @Query("SELECT * FROM consents WHERE party_id = :partyId AND status = :status AND valid_until > :now")
    Flux<Consent> findValidConsentsByPartyId(Long partyId, ConsentStatus status, LocalDateTime now);

    /**
     * Find all consents of a specific type for a party.
     *
     * @param partyId The ID of the party
     * @param consentType The type of consent
     * @return A Flux of consents
     */
    Flux<Consent> findByPartyIdAndConsentType(Long partyId, ConsentType consentType);

    /**
     * Find a valid consent by ID and party ID.
     *
     * @param id The ID of the consent
     * @param partyId The ID of the party
     * @param status The status of the consent
     * @param now The current date and time
     * @return A Mono of the consent
     */
    @Query("SELECT * FROM consents WHERE id = :id AND party_id = :partyId AND status = :status AND valid_until > :now")
    Mono<Consent> findValidConsentByIdAndPartyId(Long id, Long partyId, ConsentStatus status, LocalDateTime now);
}
