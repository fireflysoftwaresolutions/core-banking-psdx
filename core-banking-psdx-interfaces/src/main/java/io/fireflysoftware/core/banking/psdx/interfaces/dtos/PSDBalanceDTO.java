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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing an account balance according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Balance information")
public class PSDBalanceDTO {

    @Pattern(regexp = "^(closingBooked|expected|forwardAvailable|interimAvailable|interimBooked|openingBooked|previouslyClosedBooked)$",
             message = "Balance type must be a valid PSD2 balance type")
    @Schema(description = "Type of balance", example = "closingBooked")
    private String balanceType;

    @NotNull(message = "Balance amount is required")
    @Valid
    @Schema(description = "Amount information")
    private PSDAmountDTO balanceAmount;

    @Pattern(regexp = "^(CRDT|DBIT)$", message = "Credit/Debit indicator must be CRDT or DBIT")
    @Schema(description = "Credit/Debit indicator", example = "CRDT")
    private String creditDebitIndicator;

    @Schema(description = "Date and time of the balance")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime referenceDate;

    @Size(max = 100, message = "Last committed transaction must not exceed 100 characters")
    @Schema(description = "Last committed transaction included in this balance")
    private String lastCommittedTransaction;

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
}
