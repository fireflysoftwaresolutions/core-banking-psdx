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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for managing access logs according to PSD2/PSD3 and FIDA regulations.
 */
public interface AccessLogService {

    /**
     * Log an access to customer data or an operation performed on their behalf.
     *
     * @param accessLogRequest The access log request
     * @return A Mono of the created access log
     */
    Mono<PSDAccessLogDTO> logAccess(PSDAccessLogRequestDTO accessLogRequest);

    /**
     * Get an access log by its ID.
     *
     * @param logId The ID of the access log
     * @return A Mono of the access log
     */
    Mono<PSDAccessLogDTO> getAccessLog(UUID logId);

    /**
     * Get all access logs for a customer.
     *
     * @param partyId The ID of the customer
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForCustomer(UUID partyId);

    /**
     * Get all access logs for a customer within a date range.
     *
     * @param partyId The ID of the customer
     * @param fromDate The start date of the range
     * @param toDate The end date of the range
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForCustomerInDateRange(UUID partyId, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Get all access logs for a specific consent.
     *
     * @param consentId The ID of the consent
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForConsent(UUID consentId);

    /**
     * Get all access logs for a specific Third Party Provider.
     *
     * @param thirdPartyId The ID of the Third Party Provider
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForThirdParty(String thirdPartyId);

    /**
     * Count the number of access logs for a specific consent.
     *
     * @param consentId The ID of the consent
     * @return A Mono of the count
     */
    Mono<Long> countAccessLogsForConsent(UUID consentId);
}
