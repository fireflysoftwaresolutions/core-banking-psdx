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
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a Third Party Provider (TPP) according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Third Party Provider information")
public class PSDThirdPartyProviderDTO {

    @Schema(description = "Unique identifier of the TPP")
    private UUID id;

    @Schema(description = "Name of the TPP", example = "FinTech Solutions Ltd")
    private String name;

    @Schema(description = "Registration number of the TPP", example = "TPP123456")
    private String registrationNumber;

    @Schema(description = "National competent authority ID", example = "DE-BAFIN")
    private String nationalCompetentAuthority;

    @Schema(description = "National competent authority ID", example = "DE")
    private String nationalCompetentAuthorityCountry;

    @Schema(description = "Redirect URI for the TPP", example = "https://fintech-solutions.com/callback")
    private String redirectUri;

    @Schema(description = "Status of the TPP", example = "ACTIVE")
    private String status;

    @Schema(description = "Type of the TPP", example = "AISP")
    private String providerType;

    @Schema(description = "Roles of the TPP")
    private List<String> roles;

    @Schema(description = "Certificate information")
    private PSDCertificateDTO certificate;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the TPP was created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the TPP was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Inner class representing certificate information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDCertificateDTO {
        @Schema(description = "Certificate serial number", example = "12345678")
        private String serialNumber;

        @Schema(description = "Certificate subject", example = "CN=FinTech Solutions Ltd,O=FinTech Solutions,C=DE")
        private String subject;

        @Schema(description = "Certificate issuer", example = "CN=PSD2 CA,O=European Banking Authority,C=EU")
        private String issuer;

        @Schema(description = "Certificate content (Base64 encoded)", example = "MIIEpAIBAAKCAQEA...")
        private String content;

        @Schema(description = "Certificate validity from")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime validFrom;

        @Schema(description = "Certificate validity until")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime validUntil;
    }
}
