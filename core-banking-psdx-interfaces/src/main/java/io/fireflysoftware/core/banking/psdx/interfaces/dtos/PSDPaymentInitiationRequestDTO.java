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
import org.fireflyframework.annotations.ValidCurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a request to initiate a payment according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Payment initiation request")
public class PSDPaymentInitiationRequestDTO {

    @NotBlank(message = "Payment type is required")
    @Pattern(regexp = "^(sepa-credit-transfers|instant-sepa-credit-transfers|target-2-payments|cross-border-credit-transfers)$",
             message = "Payment type must be a valid PSD2 payment type")
    @Schema(description = "Type of the payment", required = true, example = "sepa-credit-transfers")
    private String paymentType;

    @Size(max = 35, message = "End-to-end identifier must not exceed 35 characters")
    @Pattern(regexp = "^[A-Za-z0-9/\\-?:().,'+\\s]*$", message = "End-to-end identifier contains invalid characters")
    @Schema(description = "End-to-end identifier", example = "E2E-ID-123")
    private String endToEndIdentification;

    @NotNull(message = "Debtor account is required")
    @Valid
    @Schema(description = "Debtor account", required = true)
    private PSDAccountReferenceDTO debtorAccount;

    @NotBlank(message = "Creditor name is required")
    @Size(max = 70, message = "Creditor name must not exceed 70 characters")
    @Schema(description = "Creditor name", required = true, example = "John Doe")
    private String creditorName;

    @NotNull(message = "Creditor account is required")
    @Valid
    @Schema(description = "Creditor account", required = true)
    private PSDAccountReferenceDTO creditorAccount;

    @Schema(description = "Creditor address")
    private PSDAddressDTO creditorAddress;

    @NotNull(message = "Amount is required")
    @Schema(description = "Payment amount information", required = true)
    private PSDAmountDTO instructedAmount;

    @Schema(description = "Remittance information unstructured", example = "Invoice 123")
    private String remittanceInformationUnstructured;

    @Schema(description = "Remittance information structured")
    private PSDRemittanceDTO remittanceInformationStructured;

    @Future(message = "Requested execution date must be in the future")
    @Schema(description = "Requested execution date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedExecutionDate;

    /**
     * Inner class representing an amount with currency.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDAmountDTO {
        @NotBlank(message = "Currency is required")
        @ValidCurrencyCode(message = "Currency must be a valid ISO 4217 currency code")
        @Schema(description = "Currency of the amount", required = true, example = "EUR")
        private String currency;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
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
     * Inner class representing an address.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PSDAddressDTO {
        @Schema(description = "Street name", example = "Main Street")
        private String street;

        @Schema(description = "Building number", example = "123")
        private String buildingNumber;

        @Schema(description = "City", example = "Berlin")
        private String city;

        @Schema(description = "Postal code", example = "10115")
        private String postalCode;

        @Schema(description = "Country", example = "DE")
        private String country;
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
