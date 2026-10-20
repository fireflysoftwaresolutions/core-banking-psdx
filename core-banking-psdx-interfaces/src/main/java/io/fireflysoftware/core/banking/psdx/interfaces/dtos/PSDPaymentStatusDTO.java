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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing the status of a payment according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Payment status information")
public class PSDPaymentStatusDTO {

    @NotNull(message = "Payment ID is required")
    @Schema(description = "Unique identifier of the payment", required = true)
    private UUID paymentId;

    @Pattern(regexp = "^(ACCP|ACSC|ACSP|ACTC|ACWC|ACWP|PDNG|RJCT|CANC)$",
             message = "Transaction status must be a valid ISO 20022 payment status")
    @Schema(description = "Status of the payment", example = "ACCP")
    private String transactionStatus;

    @Pattern(regexp = "^(ACCP|ACSC|ACSP|ACTC|ACWC|ACWP|PDNG|RJCT|CANC)$",
             message = "Status must be a valid ISO 20022 payment status")
    @Schema(description = "Status of the payment (alias for transactionStatus)", example = "ACCP")
    private String status;

    @Schema(description = "Funds availability", example = "true")
    private Boolean fundsAvailable;

    @Size(max = 500, message = "PSU message must not exceed 500 characters")
    @Schema(description = "PSU message", example = "Payment accepted")
    private String psuMessage;

    @Size(max = 500, message = "Status reason information must not exceed 500 characters")
    @Schema(description = "Additional status information", example = "Payment accepted")
    private String statusReasonInformation;

    @Valid
    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the status was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime statusUpdateDateTime;
}
