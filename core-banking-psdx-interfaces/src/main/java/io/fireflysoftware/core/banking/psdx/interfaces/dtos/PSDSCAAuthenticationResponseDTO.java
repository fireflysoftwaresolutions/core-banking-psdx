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
 * DTO representing an SCA authentication response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD SCA Authentication response")
public class PSDSCAAuthenticationResponseDTO {

    @NotBlank(message = "Challenge ID is required")
    @Size(max = 50, message = "Challenge ID must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "Challenge ID must contain only alphanumeric characters, hyphens, and underscores")
    @Schema(description = "ID of the SCA challenge", required = true, example = "sca-123456")
    private String challengeId;

    @NotBlank(message = "Authentication method is required")
    @Pattern(regexp = "^(SMS|EMAIL|PUSH|BIOMETRIC|HARDWARE_TOKEN|SOFTWARE_TOKEN)$",
             message = "Authentication method must be one of: SMS, EMAIL, PUSH, BIOMETRIC, HARDWARE_TOKEN, SOFTWARE_TOKEN")
    @Schema(description = "Authentication method used", required = true, example = "SMS")
    private String method;

    @Size(max = 100, message = "Masked target must not exceed 100 characters")
    @Schema(description = "Masked target of the authentication", example = "+49 *** *** 789")
    private String maskedTarget;

    @Positive(message = "Expires in must be a positive number")
    @Schema(description = "Expiry time of the challenge in seconds", example = "300")
    private Integer expiresIn;

    @Size(max = 500, message = "Additional info must not exceed 500 characters")
    @Schema(description = "Additional information about the challenge", example = "An SMS has been sent to your registered mobile number")
    private String additionalInfo;
}
