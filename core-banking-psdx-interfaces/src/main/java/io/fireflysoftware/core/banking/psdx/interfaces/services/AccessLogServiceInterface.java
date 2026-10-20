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
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interface for the access log service.
 */
public interface AccessLogServiceInterface {

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
    Mono<PSDAccessLogDTO> logAccess(
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
            String psuId);

    /**
     * Get access logs for a party.
     *
     * @param partyId The ID of the party (customer)
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForParty(UUID partyId);

    /**
     * Get access logs for a consent.
     *
     * @param consentId The ID of the consent
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForConsent(UUID consentId);

    /**
     * Get access logs for a third party provider.
     *
     * @param thirdPartyId The ID of the third party provider
     * @return A Flux of access logs
     */
    Flux<PSDAccessLogDTO> getAccessLogsForThirdParty(String thirdPartyId);
}
