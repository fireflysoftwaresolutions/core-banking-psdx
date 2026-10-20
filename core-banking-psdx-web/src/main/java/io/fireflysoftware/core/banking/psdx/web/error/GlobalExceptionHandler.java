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
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDConsentInvalidException;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDException;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDFormatException;
import io.fireflysoftware.core.banking.psdx.interfaces.exceptions.PSDResourceUnknownException;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilder;
import io.fireflysoftware.core.banking.psdx.web.utils.LinkBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for the application.
 * This handler catches all exceptions and returns appropriate error responses
 * according to PSD2/PSD3 standards.
 */
@Component("psdxGlobalExceptionHandler")
@Order(-2)
@Slf4j
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final LinkBuilderFactory linkBuilderFactory;

    /**
     * Constructor for GlobalExceptionHandler.
     *
     * @param errorAttributes The error attributes
     * @param resources The web properties
     * @param applicationContext The application context
     * @param serverCodecConfigurer The server codec configurer
     * @param linkBuilderFactory The link builder factory
     */
    public GlobalExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext,
            ServerCodecConfigurer serverCodecConfigurer,
            LinkBuilderFactory linkBuilderFactory) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
        this.linkBuilderFactory = linkBuilderFactory;
    }

    /**
     * Configure the routes for error handling.
     *
     * @param errorAttributes The error attributes
     * @return The router function
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Render the error response.
     *
     * @param request The server request
     * @return A Mono of ServerResponse
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Throwable error = getError(request);

        log.error("Error handling request: {}", error.getMessage(), error);

        HttpStatus status = determineHttpStatus(error);
        PSDErrorResponseDTO errorResponse = createErrorResponse(request, error, status);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    /**
     * Create a standardized error response.
     *
     * @param request The server request
     * @param error The error
     * @param status The HTTP status
     * @return The error response
     */
    private PSDErrorResponseDTO createErrorResponse(ServerRequest request, Throwable error, HttpStatus status) {
        // Get error details
        String path = request.path();
        String requestId = request.headers().firstHeader("X-Request-ID");
        String message = error.getMessage();
        if (message == null || message.isEmpty()) {
            message = status.getReasonPhrase();
        }

        // Create error code based on the error type
        String errorCode = determineErrorCode(error, status.value());

        // Create error details if available
        List<PSDErrorResponseDTO.PSDErrorDetailDTO> errors = new ArrayList<>();
        if (error instanceof PSDException) {
            PSDException psdException = (PSDException) error;
            errors.add(PSDErrorResponseDTO.PSDErrorDetailDTO.builder()
                    .code(psdException.getErrorCode())
                    .message(psdException.getMessage())
                    .build());
        } else if (error instanceof IllegalArgumentException) {
            errors.add(PSDErrorResponseDTO.PSDErrorDetailDTO.builder()
                    .code(errorCode)
                    .message(error.getMessage())
                    .build());
        } else if (error instanceof ResponseStatusException) {
            errors.add(PSDErrorResponseDTO.PSDErrorDetailDTO.builder()
                    .code(errorCode)
                    .message(((ResponseStatusException) error).getReason())
                    .build());
        }

        // Create links
        LinkBuilder linkBuilder = linkBuilderFactory.create();
        linkBuilder.withSelf(path);

        // Get TPP message if available
        String tppMessage = null;
        if (error instanceof PSDException) {
            tppMessage = ((PSDException) error).getTppMessage();
        }

        return PSDErrorResponseDTO.builder()
                .apiVersion("1.0")
                .status(status.value())
                .code(errorCode)
                .message(message)
                .detail(error.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .requestId(requestId)
                .tppMessages(tppMessage)
                ._links(linkBuilder.build())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    /**
     * Determine the HTTP status for an exception.
     *
     * @param error The exception
     * @return The HTTP status
     */
    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof IllegalArgumentException || error instanceof PSDFormatException) {
            return HttpStatus.BAD_REQUEST;
        } else if (error instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        } else if (error instanceof IllegalStateException) {
            return HttpStatus.CONFLICT;
        } else if (error instanceof PSDResourceUnknownException) {
            return HttpStatus.NOT_FOUND;
        } else if (error instanceof PSDConsentInvalidException) {
            return HttpStatus.FORBIDDEN;
        } else if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatusCode().is4xxClientError() ?
                    HttpStatus.valueOf(((ResponseStatusException) error).getStatusCode().value()) :
                    HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Determine the error code based on the error type and status.
     *
     * @param error The error
     * @param status The HTTP status
     * @return The error code
     */
    private String determineErrorCode(Throwable error, Integer status) {
        if (error instanceof PSDException) {
            return ((PSDException) error).getErrorCode();
        } else if (error instanceof IllegalArgumentException) {
            return "FORMAT_ERROR";
        } else if (error instanceof SecurityException) {
            return "PSU_CREDENTIALS_INVALID";
        } else if (error instanceof IllegalStateException) {
            return "RESOURCE_BLOCKED";
        } else if (error instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) error;
            if (responseStatusException.getStatusCode().value() == 404) {
                return "RESOURCE_UNKNOWN";
            } else if (responseStatusException.getStatusCode().value() == 401) {
                return "UNAUTHORIZED";
            } else if (responseStatusException.getStatusCode().value() == 403) {
                return "CONSENT_INVALID";
            } else if (responseStatusException.getStatusCode().value() == 400) {
                return "FORMAT_ERROR";
            } else if (responseStatusException.getStatusCode().value() == 429) {
                return "SERVICE_BLOCKED";
            }
        }

        if (status == 404) {
            return "RESOURCE_UNKNOWN";
        } else if (status == 401) {
            return "UNAUTHORIZED";
        } else if (status == 403) {
            return "CONSENT_INVALID";
        } else if (status == 400) {
            return "FORMAT_ERROR";
        } else if (status == 429) {
            return "SERVICE_BLOCKED";
        }

        return "INTERNAL_SERVER_ERROR";
    }
}
