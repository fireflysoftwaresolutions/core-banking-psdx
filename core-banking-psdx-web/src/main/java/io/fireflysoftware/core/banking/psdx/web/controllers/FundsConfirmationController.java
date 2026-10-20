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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.FundsConfirmationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for funds confirmation.
 */
@RestController
@RequestMapping("/api/v1/funds-confirmations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Funds Confirmation", description = "APIs for funds confirmation according to PSD2/PSD3 regulations")
public class FundsConfirmationController {

    private final FundsConfirmationService fundsConfirmationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Confirm funds", 
        description = """
            ## Confirm Availability of Funds

            This endpoint checks whether a specific amount of funds is available on a customer's account.

            ### Description
            This operation is part of the Funds Confirmation Service (FCS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to verify if a customer has sufficient funds for a 
            transaction without initiating an actual payment.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this funds check

            ### Request Body
            The request must include:
            * Account reference (account identifier)
            * Amount and currency to check
            * Reference information for the potential transaction

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers funds confirmation
            3. Verify that the specified account is included in the consent's scope
            4. Check the current available balance on the account
            5. Compare the available balance with the requested amount
            6. Create a funds confirmation record with the result

            ### Response
            Returns a funds confirmation object containing:
            * Confirmation identifier
            * Result (true if funds are available, false if not)
            * Timestamp of the confirmation
            * Account reference
            * Amount and currency checked
            * Reference information

            ### Important Notes
            * A positive confirmation does not guarantee funds will be available at the time of an actual payment
            * No funds are reserved or blocked by this operation
            * The confirmation is a point-in-time check and may become outdated quickly

            ### Use Cases
            * Card-based payment instruments issuing funds checks before authorizing transactions
            * Pre-validation of funds availability before initiating a payment
            * Merchant verification of customer's ability to pay

            ### Regulatory Compliance
            * This endpoint implements the Funds Confirmation Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Explicit consent is required for funds confirmation

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Funds confirmation requests are monitored for suspicious patterns
            * Results are considered sensitive financial information
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funds confirmation created successfully",
                    content = @Content(schema = @Schema(implementation = PSDFundsConfirmationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDFundsConfirmationDTO> confirmFunds(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Valid @RequestBody PSDFundsConfirmationDTO fundsConfirmationRequest) {
        log.debug("REST request to confirm funds for consent ID: {}", consentId);
        return fundsConfirmationService.confirmFunds(consentId, fundsConfirmationRequest);
    }

    @GetMapping(value = "/{fundsConfirmationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get funds confirmation", 
        description = """
            ## Retrieve a Funds Confirmation Record

            This endpoint retrieves a previously created funds confirmation record by its unique ID.

            ### Description
            This operation is part of the Funds Confirmation Service (FCS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to retrieve the details of a funds confirmation
            check that was previously performed.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes access to this funds confirmation

            ### Path Parameters
            * `fundsConfirmationId` - The unique identifier of the funds confirmation record to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers funds confirmation
            3. Verify that the funds confirmation record exists and is associated with the consent
            4. Retrieve the funds confirmation details

            ### Response
            Returns a funds confirmation object containing:
            * Confirmation identifier
            * Result (true if funds were available, false if not)
            * Timestamp of the confirmation
            * Account reference
            * Amount and currency checked
            * Reference information

            ### Historical Context
            * Funds confirmation records are historical point-in-time checks
            * The result reflects the account balance at the time of the original check
            * Current account balance may differ from the time of the check

            ### Error Scenarios
            * If the funds confirmation doesn't exist, returns a 404 Not Found response
            * If the funds confirmation exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response

            ### Regulatory Compliance
            * This endpoint implements the Funds Confirmation Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Explicit consent is required for accessing funds confirmation records

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Funds confirmation results are considered sensitive financial information
            * Access to historical records may be time-limited for security reasons
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds confirmation found",
                    content = @Content(schema = @Schema(implementation = PSDFundsConfirmationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funds confirmation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDFundsConfirmationDTO> getFundsConfirmation(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the funds confirmation", required = true)
            @PathVariable UUID fundsConfirmationId) {
        log.debug("REST request to get funds confirmation: {} for consent ID: {}", fundsConfirmationId, consentId);
        return fundsConfirmationService.getFundsConfirmation(consentId, fundsConfirmationId);
    }
}
