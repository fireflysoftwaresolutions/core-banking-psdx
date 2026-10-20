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


package io.fireflysoftware.core.banking.psdx.interfaces.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing an access log according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Access log information")
public class PSDAccessLogDTO {

    @Schema(description = "Unique identifier of the access log")
    private UUID id;

    @Schema(description = "ID of the consent used for the access")
    private UUID consentId;

    @Schema(description = "ID of the customer")
    private UUID partyId;

    @Schema(description = "ID of the third party provider")
    private String thirdPartyId;

    @Schema(description = "Type of access", example = "READ")
    private String accessType;

    @Schema(description = "Type of resource being accessed", example = "ACCOUNT")
    private String resourceType;

    @Schema(description = "ID of the resource being accessed", example = "ACC123456")
    private String resourceId;

    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent of the requester", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Status of the access", example = "SUCCESS")
    private String status;

    @Schema(description = "Error message if the access failed", example = "Unauthorized access")
    private String errorMessage;

    @Schema(description = "X-Request-ID header value", example = "99391c7e-ad88-49ec-a2ad-99ddcb1f7721")
    private String xRequestId;

    @Schema(description = "TPP-Request-ID header value", example = "TPP-123456")
    private String tppRequestId;

    @Schema(description = "PSU-ID header value", example = "PSU-123456")
    private String psuId;

    @Schema(description = "PSU-ID-Type header value", example = "email")
    private String psuIdType;

    @Schema(description = "PSU-Corporate-ID header value", example = "CORP-123456")
    private String psuCorporateId;

    @Schema(description = "PSU-Corporate-ID-Type header value", example = "email")
    private String psuCorporateIdType;

    @Schema(description = "TPP-Redirect-URI header value", example = "https://tpp.com/callback")
    private String tppRedirectUri;

    @Schema(description = "Date and time when the access was logged")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
