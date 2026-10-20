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

import io.fireflysoftware.core.banking.psdx.core.mappers.AccessLogMapper;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogServiceInterface;
import io.fireflysoftware.core.banking.psdx.models.entities.AccessLog;
import io.fireflysoftware.core.banking.psdx.models.repositories.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing access logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogService implements AccessLogServiceInterface {

    private final AccessLogRepository accessLogRepository;
    private final AccessLogMapper accessLogMapper;

    /**
     * Log an access to a resource.
     *
     * @param consentId The ID of the consent
     * @param partyId The ID of the party (customer)
     * @param thirdPartyId The ID of the third party provider
     * @param accessType The type of access
     * @param resourceType The type of resource being accessed
     * @param resourceId The ID of the resource being accessed
     * @param ipAddress The IP address of the client
     * @param userAgent The user agent of the client
     * @param status The status of the access
     * @param xRequestId The X-Request-ID header
     * @param tppRequestId The TPP-Request-ID header
     * @param psuId The PSU-ID header
     * @return A Mono of the created access log
     */
    @Override
    public Mono<PSDAccessLogDTO> logAccess(
            UUID consentId,
            UUID partyId,
            String thirdPartyId,
            AccessType accessType,
            ResourceType resourceType,
            String resourceId,
            String ipAddress,
            String userAgent,
            AccessStatus status,
            String xRequestId,
            String tppRequestId,
            String psuId) {

        log.debug("Logging access: consentId={}, partyId={}, thirdPartyId={}, accessType={}, resourceType={}, resourceId={}, status={}",
                consentId, partyId, thirdPartyId, accessType, resourceType, resourceId, status);

        AccessLog accessLog = AccessLog.builder()
                .consentId(consentId)
                .partyId(partyId)
                .thirdPartyId(thirdPartyId)
                .accessType(accessType)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .xRequestId(xRequestId)
                .tppRequestId(tppRequestId)
                .psuId(psuId)
                .timestamp(LocalDateTime.now())
                .build();

        return accessLogRepository.save(accessLog)
                .map(accessLogMapper::toDto)
                .doOnSuccess(dto -> log.info("Access logged with ID: {}", dto.getId()));
    }

    /**
     * Get access logs for a party.
     *
     * @param partyId The ID of the party (customer)
     * @return A Flux of access logs
     */
    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForParty(UUID partyId) {
        log.debug("Getting access logs for party ID: {}", partyId);

        return accessLogRepository.findByPartyIdOrderByTimestampDesc(partyId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for party ID: {}", partyId));
    }

    /**
     * Get access logs for a consent.
     *
     * @param consentId The ID of the consent
     * @return A Flux of access logs
     */
    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForConsent(UUID consentId) {
        log.debug("Getting access logs for consent ID: {}", consentId);
        
        return accessLogRepository.findByConsentIdOrderByTimestampDesc(consentId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for consent ID: {}", consentId));
    }

    /**
     * Get access logs for a third party provider.
     *
     * @param thirdPartyId The ID of the third party provider
     * @return A Flux of access logs
     */
    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForThirdParty(String thirdPartyId) {
        log.debug("Getting access logs for third party ID: {}", thirdPartyId);
        
        return accessLogRepository.findByThirdPartyIdOrderByTimestampDesc(thirdPartyId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for third party ID: {}", thirdPartyId));
    }

    /**
     * Count access logs for a consent.
     *
     * @param consentId The ID of the consent
     * @return A Mono of the count
     */
    public Mono<Long> countAccessLogsForConsent(UUID consentId) {
        log.debug("Counting access logs for consent ID: {}", consentId);

        return accessLogRepository.countByConsentId(consentId)
                .doOnSuccess(count -> log.debug("Counted {} access logs for consent ID: {}", count, consentId));
    }
}
