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
import org.fireflyframework.annotations.ValidCreditCard;
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
 * DTO representing a funds confirmation request/response according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Funds Confirmation information")
public class PSDFundsConfirmationDTO {

    @Schema(description = "Unique identifier of the funds confirmation request")
    private UUID fundsConfirmationId;

    @Schema(description = "ID of the consent used for the funds confirmation")
    private UUID consentId;

    @Schema(description = "Account reference")
    @NotNull(message = "Account is required")
    @Valid
    private PSDAccountReferenceDTO account;

    @Schema(description = "Amount information")
    @NotNull(message = "Amount is required")
    @Valid
    private PSDAmountDTO instructedAmount;

    @Size(max = 70, message = "Creditor name must not exceed 70 characters")
    @Schema(description = "Creditor name", example = "John Doe")
    private String creditorName;

    @Valid
    @Schema(description = "Creditor account")
    private PSDAccountReferenceDTO creditorAccount;

    @Schema(description = "Card number", example = "5409050000000000")
    private String cardNumber;

    @Size(max = 70, message = "PSU name must not exceed 70 characters")
    @Schema(description = "PSU name", example = "Jane Smith")
    private String psuName;

    @Schema(description = "Result of the funds confirmation", example = "true")
    private Boolean fundsAvailable;

    @Schema(description = "Date and time of the funds confirmation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime confirmationDateTime;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;


}
