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

import io.fireflysoftware.core.banking.psdx.core.ports.SCAServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationResponseDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for Strong Customer Authentication (SCA).
 */
@RestController
@RequestMapping("/api/v1/sca")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strong Customer Authentication", description = "APIs for Strong Customer Authentication according to PSD2/PSD3 regulations")
public class SCAController {

    private final SCAServicePort scaServicePort;

    /**
     * Initiate SCA for a customer.
     *
     * @param request The SCA authentication request
     * @return A Mono of PSDSCAAuthenticationResponseDTO
     */
    @PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Initiate SCA", description = "Initiates Strong Customer Authentication for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SCA initiated successfully",
                    content = @Content(schema = @Schema(implementation = PSDSCAAuthenticationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDSCAAuthenticationResponseDTO> initiateSCA(
            @Parameter(description = "SCA authentication request", required = true)
            @Valid @RequestBody PSDSCAAuthenticationRequestDTO request) {
        log.debug("REST request to initiate SCA for party ID: {}", request.getPartyId());
        return scaServicePort.initiateSCA(request);
    }

    /**
     * Validate SCA for a customer.
     *
     * @param request The SCA validation request
     * @return A Mono of PSDSCAValidationResponseDTO
     */
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate SCA", description = "Validates Strong Customer Authentication for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SCA validated successfully",
                    content = @Content(schema = @Schema(implementation = PSDSCAValidationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDSCAValidationResponseDTO> validateSCA(
            @Parameter(description = "SCA validation request", required = true)
            @Valid @RequestBody PSDSCAValidationRequestDTO request) {
        log.debug("REST request to validate SCA for challenge ID: {}", request.getChallengeId());
        return scaServicePort.validateSCA(request);
    }

    /**
     * Check if SCA is required for a payment.
     *
     * @param amount The payment amount
     * @param currency The payment currency
     * @return A Mono of Boolean indicating if SCA is required
     */
    @GetMapping(value = "/required", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check if SCA is required", description = "Checks if Strong Customer Authentication is required for a payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<Boolean> isSCARequired(
            @Parameter(description = "Payment amount", required = true)
            @RequestParam Double amount,
            @Parameter(description = "Payment currency", required = true)
            @RequestParam String currency) {
        log.debug("REST request to check if SCA is required for amount: {} {}", amount, currency);
        return scaServicePort.isSCARequired(amount, currency);
    }
}
