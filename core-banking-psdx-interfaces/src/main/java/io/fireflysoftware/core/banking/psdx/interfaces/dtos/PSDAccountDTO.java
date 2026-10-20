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
import org.fireflyframework.annotations.ValidBic;
import org.fireflyframework.annotations.ValidCurrencyCode;
import org.fireflyframework.annotations.ValidIban;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing an account according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Account information")
public class PSDAccountDTO {

    @NotNull(message = "Resource ID is required")
    @Schema(description = "Unique identifier of the account (resource ID)", required = true)
    private UUID resourceId;

    @ValidIban(message = "IBAN must be a valid International Bank Account Number")
    @Schema(description = "IBAN of the account", example = "DE89370400440532013000")
    private String iban;

    @Pattern(regexp = "^[A-Z0-9]{1,30}$", message = "BBAN must be alphanumeric and up to 30 characters")
    @Schema(description = "BBAN of the account", example = "BARC12345612345678")
    private String bban;

    @ValidCurrencyCode(message = "Currency must be a valid ISO 4217 currency code")
    @Schema(description = "Currency of the account", example = "EUR")
    private String currency;

    @Size(max = 100, message = "Account name must not exceed 100 characters")
    @Schema(description = "Name of the account as defined by the PSU", example = "Main Account")
    private String name;

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Schema(description = "Product name of the bank for this account", example = "Premium Account")
    private String product;

    @Pattern(regexp = "^(CACC|CARD|CASH|CHAR|CISH|COMM|CPAC|LLSV|LOAN|MGLD|MOMA|NREX|ODFT|ONDP|OTHR|SACC|SLRY|SVGS|TAXE|TRAD|TRAN|TRAS)$",
             message = "Cash account type must be a valid ISO 20022 account type")
    @Schema(description = "Type of the account", example = "CACC")
    private String cashAccountType;

    @Pattern(regexp = "^(enabled|disabled|deleted|blocked)$",
             message = "Account status must be one of: enabled, disabled, deleted, blocked")
    @Schema(description = "Status of the account", example = "enabled")
    private String status;

    @ValidBic(message = "BIC must be a valid Bank Identifier Code")
    @Schema(description = "BIC associated with the account", example = "DEUTDEFF")
    private String bic;

    @NotNull(message = "Owner party ID is required")
    @Schema(description = "ID of the account holder (PSU)", required = true)
    private UUID ownerPartyId;

    @Valid
    @Schema(description = "List of balances for the account")
    private List<PSDBalanceDTO> balances;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the account was last accessed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAccessedAt;

    @Schema(description = "Date and time when the account was created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the account was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
