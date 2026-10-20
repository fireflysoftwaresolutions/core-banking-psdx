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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an SCA validation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD SCA Validation response")
public class PSDSCAValidationResponseDTO {

    @NotNull(message = "Success status is required")
    @Schema(description = "Whether the validation was successful", required = true, example = "true")
    private Boolean success;

    @Size(min = 10, max = 2048, message = "Authentication token must be between 10 and 2048 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-._~+/]+=*$", message = "Authentication token must be a valid JWT or base64 encoded string")
    @Schema(description = "Authentication token for the validated session", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String authenticationToken;

    @Positive(message = "Expires in must be a positive number")
    @Schema(description = "Expiry time of the token in seconds", example = "3600")
    private Integer expiresIn;

    @Size(max = 500, message = "Error message must not exceed 500 characters")
    @Schema(description = "Error message if validation failed", example = "Invalid authentication code")
    private String errorMessage;
}
