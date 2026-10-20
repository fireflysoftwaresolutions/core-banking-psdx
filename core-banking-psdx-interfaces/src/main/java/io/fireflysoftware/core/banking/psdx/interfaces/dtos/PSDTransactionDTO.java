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
import org.fireflyframework.annotations.ValidAmount;
import org.fireflyframework.annotations.ValidBic;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing an account transaction according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Transaction information")
public class PSDTransactionDTO {

    @NotNull(message = "Transaction ID is required")
    @Schema(description = "Unique identifier of the transaction", required = true)
    private UUID transactionId;

    @Size(max = 35, message = "End-to-end identifier must not exceed 35 characters")
    @Pattern(regexp = "^[A-Za-z0-9/\\-?:().,'+\\s]*$", message = "End-to-end identifier contains invalid characters")
    @Schema(description = "End-to-end identifier", example = "E2E-ID-123")
    private String endToEndId;

    @Size(max = 35, message = "Mandate identifier must not exceed 35 characters")
    @Pattern(regexp = "^[A-Za-z0-9/\\-?:().,'+\\s]*$", message = "Mandate identifier contains invalid characters")
    @Schema(description = "Mandate identifier", example = "MANDATE-2023-10-01")
    private String mandateId;

    @Size(max = 35, message = "Creditor reference must not exceed 35 characters")
    @Pattern(regexp = "^[A-Za-z0-9/\\-?:().,'+\\s]*$", message = "Creditor reference contains invalid characters")
    @Schema(description = "Creditor reference", example = "RF18539007547034")
    private String creditorReference;

    @Pattern(regexp = "^(booked|pending|rejected|cancelled)$",
             message = "Transaction status must be one of: booked, pending, rejected, cancelled")
    @Schema(description = "Status of the transaction", example = "booked")
    private String transactionStatus;

    @Schema(description = "Booking date of the transaction")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @Schema(description = "Value date of the transaction")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;

    @Valid
    @Schema(description = "Transaction amount information")
    private PSDAmountDTO transactionAmount;

    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Exchange rate must be a valid decimal number")
    @Schema(description = "Exchange rate if applicable")
    private String exchangeRate;

    @Size(max = 70, message = "Creditor name must not exceed 70 characters")
    @Schema(description = "Creditor name", example = "John Doe")
    private String creditorName;

    @Valid
    @Schema(description = "Creditor account")
    private PSDAccountReferenceDTO creditorAccount;

    @ValidBic(message = "Creditor agent must be a valid BIC code")
    @Schema(description = "Creditor agent (BIC)", example = "DEUTDEFF")
    private String creditorAgent;

    @Schema(description = "Debtor name", example = "Jane Smith")
    private String debtorName;

    @Schema(description = "Debtor account")
    private PSDAccountReferenceDTO debtorAccount;

    @Schema(description = "Debtor agent (BIC)", example = "SOGEFRPP")
    private String debtorAgent;

    @Schema(description = "Remittance information unstructured", example = "Invoice 123")
    private String remittanceInformationUnstructured;

    @Schema(description = "Remittance information structured")
    private PSDRemittanceDTO remittanceInformationStructured;

    @Schema(description = "Purpose code", example = "OTHR")
    private String purposeCode;

    @Schema(description = "Bank transaction code", example = "PMNT-RCDT-ESCT")
    private String bankTransactionCode;

    @Schema(description = "Proprietary bank transaction code", example = "ABCD+EF-123")
    private String proprietaryBankTransactionCode;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    /**
     * Inner class representing an amount with currency.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDAmountDTO {
        @NotNull(message = "Currency is required")
        @ValidCurrencyCode(message = "Currency must be a valid ISO 4217 currency code")
        @Schema(description = "Currency of the amount", example = "EUR", required = true)
        private String currency;

        @NotNull(message = "Amount is required")
        @ValidAmount(message = "Amount must be a valid monetary amount")
        @Schema(description = "Amount value", required = true)
        private BigDecimal amount;
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

    /**
     * Inner class representing structured remittance information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDRemittanceDTO {
        @Schema(description = "Reference type", example = "SCOR")
        private String reference;

        @Schema(description = "Reference issuer", example = "CUR")
        private String referenceIssuer;

        @Schema(description = "Reference date", example = "2023-10-01")
        private LocalDate referenceDate;
    }
}
