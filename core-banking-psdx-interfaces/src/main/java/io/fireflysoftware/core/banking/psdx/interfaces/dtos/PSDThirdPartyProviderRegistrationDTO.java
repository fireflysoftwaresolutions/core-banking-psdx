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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a request to register a Third Party Provider (TPP) according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Third Party Provider registration request")
public class PSDThirdPartyProviderRegistrationDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Schema(description = "Name of the TPP", required = true, example = "FinTech Solutions Ltd")
    private String name;

    @NotBlank(message = "Registration number is required")
    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "Registration number must contain only alphanumeric characters, hyphens, and underscores")
    @Schema(description = "Registration number of the TPP", required = true, example = "TPP123456")
    private String registrationNumber;

    @NotBlank(message = "National competent authority is required")
    @Size(max = 20, message = "National competent authority must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{2}-[A-Z]+$", message = "National competent authority must follow format: CC-AUTHORITY (e.g., DE-BAFIN)")
    @Schema(description = "National competent authority ID", required = true, example = "DE-BAFIN")
    private String nationalCompetentAuthority;

    @NotBlank(message = "National competent authority country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid 2-letter ISO country code")
    @Schema(description = "National competent authority country", required = true, example = "DE")
    private String nationalCompetentAuthorityCountry;

    @NotBlank(message = "Redirect URI is required")
    @Pattern(regexp = "^https://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+$",
             message = "Redirect URI must be a valid HTTPS URL")
    @Size(max = 500, message = "Redirect URI must not exceed 500 characters")
    @Schema(description = "Redirect URI for the TPP", required = true, example = "https://fintech-solutions.com/callback")
    private String redirectUri;

    @NotBlank(message = "Provider type is required")
    @Pattern(regexp = "^(AISP|PISP|CBPII|ASPSP)$",
             message = "Provider type must be one of: AISP, PISP, CBPII, ASPSP")
    @Schema(description = "Type of the TPP", required = true, example = "AISP")
    private String providerType;

    @NotEmpty(message = "At least one role is required")
    @Size(max = 10, message = "Cannot have more than 10 roles")
    @Schema(description = "Roles of the TPP", required = true)
    private List<@Pattern(regexp = "^(PSP_AS|PSP_PI|PSP_AI|PSP_IC)$",
                          message = "Role must be one of: PSP_AS, PSP_PI, PSP_AI, PSP_IC") String> roles;

    @Valid
    @Schema(description = "Certificate information")
    private PSDCertificateDTO certificate;

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

        @Schema(description = "Certificate valid from date")
        private java.time.LocalDateTime validFrom;

        @Schema(description = "Certificate valid until date")
        private java.time.LocalDateTime validUntil;
    }
}
