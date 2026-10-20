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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an authentication response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Authentication response")
public class PSDAuthenticationResponseDTO {

    @NotBlank(message = "Access token is required")
    @Size(min = 10, max = 2048, message = "Access token must be between 10 and 2048 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-._~+/]+=*$", message = "Access token must be a valid JWT or base64 encoded string")
    @Schema(description = "Access token", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Size(min = 10, max = 2048, message = "Refresh token must be between 10 and 2048 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-._~+/]+=*$", message = "Refresh token must be a valid JWT or base64 encoded string")
    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @NotBlank(message = "Token type is required")
    @Pattern(regexp = "^(Bearer|Basic)$", message = "Token type must be Bearer or Basic")
    @Schema(description = "Token type", required = true, example = "Bearer")
    private String tokenType;

    @Positive(message = "Expires in must be a positive number")
    @Schema(description = "Expires in (seconds)", example = "3600")
    private Long expiresIn;
}
