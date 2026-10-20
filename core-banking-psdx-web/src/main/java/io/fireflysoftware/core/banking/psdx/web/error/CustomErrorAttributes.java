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


package io.fireflysoftware.core.banking.psdx.web.error;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDErrorResponseDTO;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilder;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom error attributes for the application.
 * This class customizes the error attributes returned in error responses.
 * It formats errors according to PSD2/PSD3 standards.
 */
@Component
@RequiredArgsConstructor
public class CustomErrorAttributes extends DefaultErrorAttributes {

    private final LinkBuilderFactory linkBuilderFactory;

    /**
     * Get the error attributes.
     *
     * @param request The server request
     * @param options The error attribute options
     * @return The error attributes
     */
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        // Create a standardized error response
        PSDErrorResponseDTO errorResponse = createErrorResponse(request, errorAttributes);

        // Convert the error response to a map
        Map<String, Object> result = convertToMap(errorResponse);

        // Remove sensitive information
        result.remove("trace");

        return result;
    }

    /**
     * Create a standardized error response.
     *
     * @param request The server request
     * @param errorAttributes The error attributes
     * @return The error response
     */
    private PSDErrorResponseDTO createErrorResponse(ServerRequest request, Map<String, Object> errorAttributes) {
        Throwable error = getError(request);

        // Get error details
        Integer status = (Integer) errorAttributes.get("status");
        String message = (String) errorAttributes.get("message");
        String path = (String) errorAttributes.get("path");
        String requestId = request.headers().firstHeader("X-Request-ID");

        // Create error code based on the error type
        String errorCode = determineErrorCode(error, status);

        // Create error details if available
        List<PSDErrorResponseDTO.PSDErrorDetailDTO> errors = new ArrayList<>();
        if (error instanceof IllegalArgumentException) {
            errors.add(PSDErrorResponseDTO.PSDErrorDetailDTO.builder()
                    .code(errorCode)
                    .message(error.getMessage())
                    .build());
        }

        // Create links
        LinkBuilder linkBuilder = linkBuilderFactory.create();
        linkBuilder.withSelf(path);

        return PSDErrorResponseDTO.builder()
                .apiVersion("1.0")
                .status(status)
                .code(errorCode)
                .message(message)
                .detail(error.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .requestId(requestId)
                ._links(linkBuilder.build())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    /**
     * Determine the error code based on the error type and status.
     *
     * @param error The error
     * @param status The HTTP status
     * @return The error code
     */
    private String determineErrorCode(Throwable error, Integer status) {
        if (error instanceof IllegalArgumentException) {
            return "FORMAT_ERROR";
        } else if (error instanceof SecurityException) {
            return "PSU_CREDENTIALS_INVALID";
        } else if (error instanceof IllegalStateException) {
            return "RESOURCE_BLOCKED";
        } else if (status == 404) {
            return "RESOURCE_UNKNOWN";
        } else if (status == 401) {
            return "UNAUTHORIZED";
        } else if (status == 403) {
            return "CONSENT_INVALID";
        } else if (status == 400) {
            return "FORMAT_ERROR";
        } else if (status == 429) {
            return "SERVICE_BLOCKED";
        } else {
            return "INTERNAL_SERVER_ERROR";
        }
    }

    /**
     * Convert an error response to a map.
     *
     * @param errorResponse The error response
     * @return The map
     */
    private Map<String, Object> convertToMap(PSDErrorResponseDTO errorResponse) {
        // This is a simplified implementation
        // In a real application, you would use a proper object mapper
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("apiVersion", errorResponse.getApiVersion());
        result.put("status", errorResponse.getStatus());
        result.put("code", errorResponse.getCode());
        result.put("message", errorResponse.getMessage());
        result.put("detail", errorResponse.getDetail());
        result.put("timestamp", errorResponse.getTimestamp());
        result.put("path", errorResponse.getPath());
        result.put("requestId", errorResponse.getRequestId());
        result.put("_links", errorResponse.get_links());
        result.put("errors", errorResponse.getErrors());
        return result;
    }
}
