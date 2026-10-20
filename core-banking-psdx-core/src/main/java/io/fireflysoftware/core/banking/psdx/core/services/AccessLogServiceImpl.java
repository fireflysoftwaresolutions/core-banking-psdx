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
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogService;
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
 * Implementation of the AccessLogService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;
    private final AccessLogMapper accessLogMapper;

    @Override
    public Mono<PSDAccessLogDTO> logAccess(PSDAccessLogRequestDTO accessLogRequest) {
        log.debug("Logging access for consent ID: {} and party ID: {}",
                accessLogRequest.getConsentId(), accessLogRequest.getPartyId());

        AccessLog accessLog = AccessLog.builder()
                .consentId(accessLogRequest.getConsentId())
                .partyId(accessLogRequest.getPartyId())
                .thirdPartyId(accessLogRequest.getThirdPartyId())
                .accessType(AccessType.valueOf(accessLogRequest.getAccessType()))
                .resourceType(ResourceType.valueOf(accessLogRequest.getResourceType()))
                .resourceId(accessLogRequest.getResourceId())
                .ipAddress(accessLogRequest.getIpAddress())
                .userAgent(accessLogRequest.getUserAgent())
                .status(AccessStatus.valueOf(accessLogRequest.getStatus()))
                .errorMessage(accessLogRequest.getErrorMessage())
                .xRequestId(accessLogRequest.getXRequestId())
                .timestamp(LocalDateTime.now())
                .build();

        return accessLogRepository.save(accessLog)
                .map(accessLogMapper::toDto)
                .doOnSuccess(dto -> log.info("Access logged with ID: {}", dto.getId()));
    }

    @Override
    public Mono<PSDAccessLogDTO> getAccessLog(UUID logId) {
        log.debug("Getting access log with ID: {}", logId);

        return accessLogRepository.findById(logId)
                .map(accessLogMapper::toDto)
                .doOnSuccess(dto -> {
                    if (dto != null) {
                        log.debug("Found access log with ID: {}", logId);
                    } else {
                        log.debug("Access log not found with ID: {}", logId);
                    }
                });
    }

    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForCustomer(UUID partyId) {
        log.debug("Getting access logs for party ID: {}", partyId);

        return accessLogRepository.findByPartyId(partyId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for party ID: {}", partyId));
    }

    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForCustomerInDateRange(UUID partyId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("Getting access logs for party ID: {} between {} and {}",
                partyId, fromDate, toDate);

        return accessLogRepository.findByPartyIdAndDateRange(partyId, fromDate, toDate)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for party ID: {} in date range", partyId));
    }

    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForConsent(UUID consentId) {
        log.debug("Getting access logs for consent ID: {}", consentId);

        return accessLogRepository.findByConsentId(consentId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for consent ID: {}", consentId));
    }

    @Override
    public Flux<PSDAccessLogDTO> getAccessLogsForThirdParty(String thirdPartyId) {
        log.debug("Getting access logs for third party ID: {}", thirdPartyId);

        return accessLogRepository.findByThirdPartyId(thirdPartyId)
                .map(accessLogMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved access logs for third party ID: {}", thirdPartyId));
    }

    @Override
    public Mono<Long> countAccessLogsForConsent(UUID consentId) {
        log.debug("Counting access logs for consent ID: {}", consentId);

        return accessLogRepository.countByConsentId(consentId)
                .doOnSuccess(count -> log.debug("Counted {} access logs for consent ID: {}", count, consentId));
    }
}
