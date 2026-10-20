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

import io.fireflysoftware.core.banking.psdx.core.mappers.ConsentMapper;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.models.entities.Consent;
import io.fireflysoftware.core.banking.psdx.models.repositories.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the ConsentService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepository;
    private final ConsentMapper consentMapper;

    @Override
    public Mono<PSDConsentDTO> createConsent(PSDConsentRequestDTO consentRequest) {
        log.debug("Creating consent for party ID: {}", consentRequest.getPartyId());

        Consent consent = Consent.builder()
                .partyId(UUID.fromString(consentRequest.getPartyId()))
                .consentType(ConsentType.valueOf(consentRequest.getConsentType()))
                .status(ConsentStatus.RECEIVED)
                .validFrom(consentRequest.getValidFrom() != null ? consentRequest.getValidFrom() : LocalDateTime.now())
                .validUntil(consentRequest.getValidUntil())
                .accessFrequency(consentRequest.getAccessFrequency())
                .accessScope(consentRequest.getAccessScope())
                .build();

        return consentRepository.save(consent)
                .map(consentMapper::toDto)
                .doOnSuccess(dto -> log.info("Consent created with ID: {}", dto.getId()));
    }

    @Override
    public Mono<PSDConsentDTO> getConsent(UUID consentId) {
        log.debug("Getting consent with ID: {}", consentId);

        return consentRepository.findById(consentId)
                .map(consentMapper::toDto)
                .doOnSuccess(dto -> {
                    if (dto != null) {
                        log.debug("Found consent with ID: {}", consentId);
                    } else {
                        log.debug("Consent not found with ID: {}", consentId);
                    }
                });
    }

    @Override
    public Flux<PSDConsentDTO> getConsentsForCustomer(UUID partyId) {
        log.debug("Getting consents for party ID: {}", partyId);

        return consentRepository.findByPartyId(partyId)
                .map(consentMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved consents for party ID: {}", partyId));
    }

    @Override
    public Mono<PSDConsentDTO> updateConsentStatus(UUID consentId, PSDConsentStatusDTO statusUpdate) {
        log.debug("Updating consent status for ID: {} to {}", consentId, statusUpdate.getStatus());

        return consentRepository.findById(consentId)
                .flatMap(consent -> {
                    consent.setStatus(ConsentStatus.valueOf(statusUpdate.getStatus()));
                    consent.setUpdatedAt(LocalDateTime.now());
                    return consentRepository.save(consent);
                })
                .map(consentMapper::toDto)
                .doOnSuccess(dto -> log.info("Updated consent status for ID: {} to {}", consentId, statusUpdate.getStatus()));
    }

    @Override
    public Mono<PSDConsentDTO> revokeConsent(UUID consentId) {
        log.debug("Revoking consent with ID: {}", consentId);

        return consentRepository.findById(consentId)
                .flatMap(consent -> {
                    consent.setStatus(ConsentStatus.REVOKED);
                    consent.setUpdatedAt(LocalDateTime.now());
                    return consentRepository.save(consent);
                })
                .map(consentMapper::toDto)
                .doOnSuccess(dto -> log.info("Revoked consent with ID: {}", consentId));
    }

    @Override
    public Mono<Boolean> validateConsent(UUID consentId, String resourceType, String accessType) {
        log.debug("Validating consent with ID: {} for resource type: {} and access type: {}",
                consentId, resourceType, accessType);

        return consentRepository.findById(consentId)
                .map(consent -> {
                    // Check if consent is valid
                    boolean isValid = consent.getStatus() == ConsentStatus.VALID &&
                            consent.getValidFrom().isBefore(LocalDateTime.now()) &&
                            consent.getValidUntil().isAfter(LocalDateTime.now()) &&
                            consent.getAccessScope().contains(resourceType.toLowerCase());

                    log.debug("Consent validation result for ID {}: {}", consentId, isValid);
                    return isValid;
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<PSDConsentStatusDTO> getConsentStatus(UUID consentId) {
        log.debug("Getting status for consent with ID: {}", consentId);

        return consentRepository.findById(consentId)
                .map(consent -> {
                    PSDConsentStatusDTO statusDTO = new PSDConsentStatusDTO();
                    statusDTO.setConsentStatus(consent.getStatus().name());
                    return statusDTO;
                })
                .doOnSuccess(statusDTO -> {
                    if (statusDTO != null) {
                        log.debug("Retrieved status for consent with ID: {}: {}", consentId, statusDTO.getConsentStatus());
                    } else {
                        log.debug("No consent found with ID: {}", consentId);
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Consent not found with ID: " + consentId)));
    }
}
