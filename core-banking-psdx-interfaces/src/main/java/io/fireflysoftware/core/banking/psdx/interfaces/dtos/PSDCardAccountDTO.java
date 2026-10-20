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
import org.fireflyframework.annotations.ValidCurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a card account according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Card Account information")
public class PSDCardAccountDTO {

    @NotNull(message = "Resource ID is required")
    @Schema(description = "Unique identifier of the card account (resource ID)", required = true)
    private UUID resourceId;

    @Pattern(regexp = "^[0-9*]{12,19}$", message = "Masked PAN must be 12-19 characters with digits and asterisks")
    @Schema(description = "Masked PAN of the card", example = "540905******0000")
    private String maskedPan;

    @ValidCurrencyCode(message = "Currency must be a valid ISO 4217 currency code")
    @Schema(description = "Currency of the card account", example = "EUR")
    private String currency;

    @Size(max = 100, message = "Card account name must not exceed 100 characters")
    @Schema(description = "Name of the card account as defined by the PSU", example = "Main Card")
    private String name;

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Schema(description = "Product name of the bank for this card account", example = "Premium Card")
    private String product;

    @Pattern(regexp = "^(enabled|disabled|deleted|blocked)$",
             message = "Card account status must be one of: enabled, disabled, deleted, blocked")
    @Schema(description = "Status of the card account", example = "enabled")
    private String status;

    @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$", message = "Credit limit must be a valid monetary amount")
    @Schema(description = "Credit limit of the card", example = "5000.00")
    private String creditLimit;

    @NotNull(message = "Owner party ID is required")
    @Schema(description = "ID of the card holder (PSU)", required = true)
    private UUID ownerPartyId;

    @Pattern(regexp = "^(debit|credit|prepaid)$",
             message = "Card type must be one of: debit, credit, prepaid")
    @Schema(description = "Card type", example = "debit")
    private String cardType;

    @Pattern(regexp = "^(visa|mastercard|amex|discover|jcb|diners|unionpay)$",
             message = "Card network must be a valid card network")
    @Schema(description = "Card network", example = "visa")
    private String cardNetwork;

    @Size(max = 100, message = "Card issuer name must not exceed 100 characters")
    @Schema(description = "Card issuer name", example = "Example Bank")
    private String cardIssuerName;

    @Size(max = 50, message = "Card issuer ID must not exceed 50 characters")
    @Schema(description = "Card issuer ID", example = "EXAMPLEBANK")
    private String cardIssuerId;

    @Schema(description = "Card expiry date")
    @JsonFormat(pattern = "yyyy-MM")
    private LocalDate expiryDate;

    @Schema(description = "List of balances for the card account")
    private List<PSDBalanceDTO> balances;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the card account was last accessed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAccessedAt;

    @Schema(description = "Date and time when the card account was created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the card account was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
