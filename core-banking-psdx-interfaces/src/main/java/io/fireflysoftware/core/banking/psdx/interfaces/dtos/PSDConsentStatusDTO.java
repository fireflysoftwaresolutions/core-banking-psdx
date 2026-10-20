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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing the status of a consent according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Consent status information")
public class PSDConsentStatusDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(received|valid|rejected|expired|revoked|terminated)$",
             message = "Consent status must be one of: received, valid, rejected, expired, revoked, terminated")
    @Schema(description = "Status of the consent", required = true, example = "valid")
    private String consentStatus;

    @Pattern(regexp = "^(received|valid|rejected|expired|revoked|terminated)$",
             message = "Status must be one of: received, valid, rejected, expired, revoked, terminated")
    @Schema(description = "Status of the consent (alias for consentStatus)", example = "valid")
    private String status;

    @Size(max = 500, message = "PSU message must not exceed 500 characters")
    @Schema(description = "PSU message", example = "Consent accepted")
    private String psuMessage;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "Date and time when the status was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime statusUpdateDateTime;
}
