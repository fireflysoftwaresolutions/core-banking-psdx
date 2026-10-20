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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing an error response according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PSD Error response")
public class PSDErrorResponseDTO {

    @Schema(description = "API version", example = "1.0")
    private String apiVersion;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Error code", example = "FORMAT_ERROR")
    private String code;

    @Schema(description = "Error message", example = "Format of certain request fields are not matching the XS2A requirements.")
    private String message;

    @Schema(description = "Detailed error description", example = "The provided account number is not valid.")
    private String detail;

    @Schema(description = "Timestamp of the error")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Path of the request", example = "/api/accounts/12345")
    private String path;

    @Schema(description = "Request ID", example = "99391c7e-ad88-49ec-a2ad-99ddcb1f7721")
    private String requestId;

    @Schema(description = "TPP message information")
    private String tppMessages;

    @Schema(description = "Links to related resources")
    private PSDLinksDTO _links;

    @Schema(description = "List of detailed errors")
    private List<PSDErrorDetailDTO> errors;

    /**
     * DTO representing a detailed error.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PSD Error detail")
    public static class PSDErrorDetailDTO {

        @Schema(description = "Error code", example = "FORMAT_ERROR")
        private String code;

        @Schema(description = "Error message", example = "Format of certain request fields are not matching the XS2A requirements.")
        private String message;

        @Schema(description = "Path to the field that caused the error", example = "account.iban")
        private String path;
    }
}
