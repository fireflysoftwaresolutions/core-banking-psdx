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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogService;
import io.fireflysoftware.core.banking.psdx.models.entities.Consent;
import io.fireflysoftware.core.banking.psdx.models.repositories.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for validating consents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentValidationService {

    private final ConsentRepository consentRepository;
    private final AccessLogService accessLogService;

    /**
     * Validate a consent for a specific resource type.
     *
     * @param consentId The ID of the consent
     * @param resourceType The type of resource being accessed
     * @param partyId The ID of the party (customer)
     * @param thirdPartyId The ID of the third party provider
     * @return A Mono of Boolean indicating if the consent is valid
     */
    public Mono<Boolean> validateConsent(UUID consentId, ResourceType resourceType, UUID partyId, String thirdPartyId) {
        log.debug("Validating consent ID: {} for resource type: {}, party ID: {}, third party ID: {}",
                consentId, resourceType, partyId, thirdPartyId);

        return consentRepository.findById(consentId)
                .flatMap(consent -> validateConsent(consent, resourceType, partyId, thirdPartyId));
    }

    /**
     * Validate a consent for a specific resource type.
     *
     * @param consent The consent
     * @param resourceType The type of resource being accessed
     * @param partyId The ID of the party (customer)
     * @param thirdPartyId The ID of the third party provider
     * @return A Mono of Boolean indicating if the consent is valid
     */
    private Mono<Boolean> validateConsent(Consent consent, ResourceType resourceType, UUID partyId, String thirdPartyId) {
        // Check if the consent is valid
        if (consent.getStatus() != ConsentStatus.VALID) {
            log.warn("Consent ID: {} has invalid status: {}", consent.getId(), consent.getStatus());
            return Mono.just(false);
        }
        
        // Check if the consent is expired
        LocalDateTime now = LocalDateTime.now();
        if (consent.getValidUntil().isBefore(now)) {
            log.warn("Consent ID: {} is expired, valid until: {}", consent.getId(), consent.getValidUntil());
            return Mono.just(false);
        }
        
        // Check if the consent is for the correct party
        if (!consent.getPartyId().equals(partyId)) {
            log.warn("Consent ID: {} is for party ID: {}, but request is for party ID: {}", 
                    consent.getId(), consent.getPartyId(), partyId);
            return Mono.just(false);
        }
        
        // Check if the consent is for the correct resource type
        boolean isValidResourceType = isValidResourceTypeForConsent(consent.getConsentType(), resourceType);
        if (!isValidResourceType) {
            log.warn("Consent ID: {} of type: {} is not valid for resource type: {}", 
                    consent.getId(), consent.getConsentType(), resourceType);
            return Mono.just(false);
        }
        
        // Check if the consent has been used too many times
        if (consent.getAccessFrequency() != null && consent.getAccessFrequency() > 0) {
            return accessLogService.countAccessLogsForConsent(consent.getId())
                    .flatMap(count -> {
                        if (count >= consent.getAccessFrequency()) {
                            log.warn("Consent ID: {} has been used too many times: {}/{}", 
                                    consent.getId(), count, consent.getAccessFrequency());
                            return Mono.just(false);
                        }
                        return updateLastActionDateAndReturnTrue(consent);
                    });
        }
        
        return updateLastActionDateAndReturnTrue(consent);
    }

    /**
     * Check if a resource type is valid for a consent type.
     *
     * @param consentType The consent type
     * @param resourceType The resource type
     * @return True if the resource type is valid for the consent type, false otherwise
     */
    private boolean isValidResourceTypeForConsent(ConsentType consentType, ResourceType resourceType) {
        switch (consentType) {
            case ACCOUNT_INFORMATION:
                return resourceType == ResourceType.ACCOUNT || 
                       resourceType == ResourceType.BALANCE || 
                       resourceType == ResourceType.TRANSACTION;
            case PAYMENT_INITIATION:
                return resourceType == ResourceType.PAYMENT;
            case FUNDS_CONFIRMATION:
                return resourceType == ResourceType.FUNDS_CONFIRMATION;
            case CARD_INFORMATION:
                return resourceType == ResourceType.CARD || 
                       resourceType == ResourceType.CARD_BALANCE || 
                       resourceType == ResourceType.CARD_TRANSACTION;
            default:
                return false;
        }
    }

    /**
     * Update the last action date of a consent and return true.
     *
     * @param consent The consent
     * @return A Mono of Boolean with value true
     */
    private Mono<Boolean> updateLastActionDateAndReturnTrue(Consent consent) {
        consent.setLastActionDate(LocalDateTime.now());
        return consentRepository.save(consent)
                .thenReturn(true);
    }
}
