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
 * DTO representing a consent according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Consent information")
public class PSDConsentDTO {

    @Schema(description = "Unique identifier of the consent")
    private UUID id;

    @Schema(description = "ID of the customer who gave the consent")
    private UUID partyId;

    @Schema(description = "Type of consent", example = "account")
    private String consentType;

    @Schema(description = "Status of the consent", example = "valid")
    private String consentStatus;

    @Schema(description = "Date and time from which the consent is valid")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "Date and time until which the consent is valid")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validUntil;

    @Schema(description = "Frequency of access per day", example = "4")
    private Integer frequencyPerDay;

    @Schema(description = "Access frequency (alias for frequencyPerDay)", example = "4")
    private Integer accessFrequency;

    @Schema(description = "Access scope", example = "all-accounts")
    private String accessScope;

    @Schema(description = "Last date of usage")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastActionDate;

    @Schema(description = "Access permissions")
    private List<PSDAccessDTO> access;

    @Schema(description = "Flag indicating if combined service is allowed", example = "false")
    private Boolean combinedServiceIndicator;

    @Schema(description = "Flag indicating if recurring indicator", example = "true")
    private Boolean recurringIndicator;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the consent was created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the consent was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Inner class representing access permissions.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDAccessDTO {
        @Schema(description = "Resource ID", example = "account-123")
        private String resourceId;

        @Schema(description = "Type of access", example = "account")
        private String type;

        @Schema(description = "Accounts that can be accessed")
        private List<PSDAccountReferenceDTO> accounts;

        @Schema(description = "Balances that can be accessed")
        private List<PSDAccountReferenceDTO> balances;

        @Schema(description = "Transactions that can be accessed")
        private List<PSDAccountReferenceDTO> transactions;

        @Schema(description = "Available accounts consent", example = "allAccounts")
        private String availableAccounts;

        @Schema(description = "Available accounts with balances consent", example = "allAccounts")
        private String availableAccountsWithBalances;

        @Schema(description = "All PSD2 consent", example = "allAccounts")
        private String allPsd2;
    }

    /**
     * Inner class representing an account reference.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDAccountReferenceDTO {
        @Schema(description = "IBAN of the account", example = "DE89370400440532013000")
        private String iban;

        @Schema(description = "BBAN of the account", example = "BARC12345612345678")
        private String bban;

        @Schema(description = "PAN of the card", example = "5409050000000000")
        private String pan;

        @Schema(description = "Masked PAN of the card", example = "540905******0000")
        private String maskedPan;

        @Schema(description = "MSISDN of the account", example = "+49 170 1234567")
        private String msisdn;

        @Schema(description = "Currency of the account", example = "EUR")
        private String currency;
    }
}
