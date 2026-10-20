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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an SCA validation request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD SCA Validation request")
public class PSDSCAValidationRequestDTO {

    @NotBlank(message = "Challenge ID is required")
    @Size(max = 50, message = "Challenge ID must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "Challenge ID must contain only alphanumeric characters, hyphens, and underscores")
    @Schema(description = "ID of the SCA challenge", required = true, example = "sca-123456")
    private String challengeId;

    @NotBlank(message = "Authentication code is required")
    @Size(min = 4, max = 10, message = "Authentication code must be between 4 and 10 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Authentication code must contain only digits")
    @Schema(description = "Authentication code provided by the customer", required = true, example = "123456")
    private String authenticationCode;
}
