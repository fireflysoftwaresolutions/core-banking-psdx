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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a request to log an access according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Access log request")
public class PSDAccessLogRequestDTO {

    @NotNull(message = "Consent ID is required")
    @Schema(description = "ID of the consent used for the access", required = true)
    private UUID consentId;

    @NotNull(message = "Party ID is required")
    @Schema(description = "ID of the customer", required = true)
    private UUID partyId;

    @NotBlank(message = "Third party ID is required")
    @Size(max = 100, message = "Third party ID must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "Third party ID must contain only alphanumeric characters, hyphens, and underscores")
    @Schema(description = "ID of the third party provider", required = true)
    private String thirdPartyId;

    @NotBlank(message = "Access type is required")
    @Pattern(regexp = "^(READ|WRITE|DELETE)$", message = "Access type must be one of: READ, WRITE, DELETE")
    @Schema(description = "Type of access", required = true, example = "READ")
    private String accessType;

    @NotBlank(message = "Resource type is required")
    @Pattern(regexp = "^(ACCOUNT|PAYMENT|CONSENT|BALANCE|TRANSACTION|FUNDS_CONFIRMATION)$",
             message = "Resource type must be one of: ACCOUNT, PAYMENT, CONSENT, BALANCE, TRANSACTION, FUNDS_CONFIRMATION")
    @Schema(description = "Type of resource being accessed", required = true, example = "ACCOUNT")
    private String resourceType;

    @NotBlank(message = "Resource ID is required")
    @Size(max = 100, message = "Resource ID must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "Resource ID must contain only alphanumeric characters, hyphens, and underscores")
    @Schema(description = "ID of the resource being accessed", required = true, example = "ACC123456")
    private String resourceId;

    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
             message = "IP address must be a valid IPv4 or IPv6 address")
    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent of the requester", example = "Mozilla/5.0")
    private String userAgent;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the access", required = true, example = "SUCCESS")
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
}
