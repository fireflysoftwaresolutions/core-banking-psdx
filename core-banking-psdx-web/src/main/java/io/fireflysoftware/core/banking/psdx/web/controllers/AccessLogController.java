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


package io.fireflysoftware.core.banking.psdx.web.controllers;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * REST controller for access logging.
 */
@RestController
@RequestMapping("/api/v1/access-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Access Logging", description = "APIs for access logging according to PSD2/PSD3 and FIDA regulations")
public class AccessLogController {

    private final AccessLogService accessLogService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log access", description = "Logs an access to customer data or an operation performed on their behalf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Access logged successfully",
                    content = @Content(schema = @Schema(implementation = PSDAccessLogDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDAccessLogDTO> logAccess(
            @Valid @RequestBody PSDAccessLogRequestDTO accessLogRequest) {
        log.debug("REST request to log access for consent ID: {} and party ID: {}",
                accessLogRequest.getConsentId(), accessLogRequest.getPartyId());
        return accessLogService.logAccess(accessLogRequest);
    }

    @GetMapping(value = "/{logId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get access log", description = "Gets an access log by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access log found",
                    content = @Content(schema = @Schema(implementation = PSDAccessLogDTO.class))),
            @ApiResponse(responseCode = "404", description = "Access log not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDAccessLogDTO> getAccessLog(
            @Parameter(description = "ID of the access log", required = true)
            @PathVariable UUID logId) {
        log.debug("REST request to get access log: {}", logId);
        return accessLogService.getAccessLog(logId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get access logs", description = "Gets access logs based on query parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access logs found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PSDAccessLogDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDAccessLogDTO> getAccessLogs(
            @Parameter(description = "ID of the customer")
            @RequestParam(required = false) UUID partyId,
            @Parameter(description = "ID of the consent")
            @RequestParam(required = false) UUID consentId,
            @Parameter(description = "ID of the third party provider")
            @RequestParam(required = false) String thirdPartyId,
            @Parameter(description = "Start date of the range")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date of the range")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.debug("REST request to get access logs with filters: partyId={}, consentId={}, thirdPartyId={}, fromDate={}, toDate={}",
                partyId, consentId, thirdPartyId, fromDate, toDate);

        if (partyId != null && fromDate != null && toDate != null) {
            return accessLogService.getAccessLogsForCustomerInDateRange(partyId, fromDate, toDate);
        } else if (partyId != null) {
            return accessLogService.getAccessLogsForCustomer(partyId);
        } else if (consentId != null) {
            return accessLogService.getAccessLogsForConsent(consentId);
        } else if (thirdPartyId != null) {
            return accessLogService.getAccessLogsForThirdParty(thirdPartyId);
        } else {
            return Flux.empty();
        }
    }
}
