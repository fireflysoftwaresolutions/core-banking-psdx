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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing an exchange rate according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Exchange Rate information")
public class PSDReportExchangeRateDTO {

    @NotNull(message = "Source currency is required")
    @ValidCurrencyCode(message = "Source currency must be a valid ISO 4217 currency code")
    @Schema(description = "Source currency", required = true, example = "EUR")
    private String sourceCurrency;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be positive")
    @Schema(description = "Exchange rate", required = true, example = "1.1234")
    private BigDecimal exchangeRate;

    @ValidCurrencyCode(message = "Unit currency must be a valid ISO 4217 currency code")
    @Schema(description = "Unit currency", example = "EUR")
    private String unitCurrency;

    @NotNull(message = "Target currency is required")
    @ValidCurrencyCode(message = "Target currency must be a valid ISO 4217 currency code")
    @Schema(description = "Target currency", required = true, example = "USD")
    private String targetCurrency;

    @NotNull(message = "Quotation date is required")
    @Schema(description = "Quotation date", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime quotationDate;
}
