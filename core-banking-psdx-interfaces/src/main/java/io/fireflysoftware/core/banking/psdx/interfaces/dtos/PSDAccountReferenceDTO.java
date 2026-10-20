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

import org.fireflyframework.annotations.ValidBic;
import org.fireflyframework.annotations.ValidCreditCard;
import org.fireflyframework.annotations.ValidCurrencyCode;
import org.fireflyframework.annotations.ValidIban;
import org.fireflyframework.annotations.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an account reference according to PSD2/PSD3 standards.
 * This is used to identify an account in a standardized way.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Account reference information")
public class PSDAccountReferenceDTO {

    @ValidIban(message = "IBAN must be a valid International Bank Account Number")
    @Schema(description = "IBAN of the account", example = "DE89370400440532013000")
    private String iban;

    @Pattern(regexp = "^[A-Z0-9]{1,30}$", message = "BBAN must be alphanumeric and up to 30 characters")
    @Schema(description = "BBAN of the account", example = "BARC12345612345678")
    private String bban;

    @Schema(description = "PAN of the card", example = "5409050000000000")
    private String pan;

    @Pattern(regexp = "^[0-9*]{12,19}$", message = "Masked PAN must be 12-19 characters with digits and asterisks")
    @Schema(description = "Masked PAN of the card", example = "540905******0000")
    private String maskedPan;

    @Schema(description = "MSISDN of the account", example = "+49 170 1234567")
    private String msisdn;

    @ValidCurrencyCode(message = "Currency must be a valid ISO 4217 currency code")
    @Schema(description = "Currency of the account", example = "EUR")
    private String currency;
}
