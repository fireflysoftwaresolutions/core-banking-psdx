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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderRegistrationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ThirdPartyProviderService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;



/**
 * REST controller for Third Party Provider (TPP) management.
 */
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Third Party Provider Management", description = "APIs for managing Third Party Providers according to PSD2/PSD3 regulations")
public class ThirdPartyProviderController {

    private final ThirdPartyProviderService thirdPartyProviderService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register provider", 
        description = """
            ## Register a new Third Party Provider

            This endpoint allows for the registration of a new Third Party Provider (TPP) in the system.

            ### Description
            TPPs are entities that provide payment initiation or account information services to customers.
            Registration is required before a TPP can access any PSD2/PSD3 APIs.

            ### Request Details
            The request must include:
            * TPP name and identification details
            * Contact information
            * Regulatory information including authorization number
            * Certificate details for secure communication

            ### Processing
            The system will:
            1. Validate the TPP's registration information
            2. Check for existing registrations to prevent duplicates
            3. Generate a unique API key for the TPP
            4. Store the TPP's information securely

            ### Response
            Returns the registered TPP information including the generated API key.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Provider registered successfully",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> registerProvider(
            @Valid @RequestBody PSDThirdPartyProviderRegistrationDTO registration) {
        log.debug("REST request to register provider: {}", registration.getName());
        return thirdPartyProviderService.registerProvider(registration);
    }

    @GetMapping(value = "/{providerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get provider", 
        description = """
            ## Retrieve a Third Party Provider

            This endpoint retrieves detailed information about a specific Third Party Provider (TPP) by its unique identifier.

            ### Description
            This operation allows authorized users to access the complete profile of a registered TPP.

            ### Path Parameters
            * `providerId` - The unique identifier of the TPP to retrieve

            ### Processing
            The system will:
            1. Validate the provided TPP ID
            2. Check if the TPP exists in the system
            3. Verify that the requester has permission to view the TPP information

            ### Response
            Returns the complete TPP profile including:
            * Basic information (name, ID, status)
            * Contact details
            * Regulatory information
            * Registration date
            * Current status (active, suspended, etc.)

            ### Security Considerations
            Access to this endpoint should be restricted to authorized personnel only.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider found",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> getProvider(
            @Parameter(description = "ID of the provider", required = true)
            @PathVariable UUID providerId) {
        log.debug("REST request to get provider: {}", providerId);
        return thirdPartyProviderService.getProvider(providerId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get all providers", 
        description = """
            ## Retrieve All Third Party Providers

            This endpoint retrieves a list of all registered Third Party Providers (TPPs) in the system.

            ### Description
            This operation allows authorized users to access a comprehensive list of all TPPs registered 
            with the system. This is useful for administrative purposes, reporting, and monitoring.

            ### Query Parameters
            No specific query parameters are required for this endpoint.

            ### Processing
            The system will:
            1. Verify that the requester has permission to view TPP information
            2. Retrieve all TPP records from the database
            3. Format the response as a collection of TPP objects

            ### Response
            Returns an array of TPP profiles, each containing:
            * Basic information (name, ID, status)
            * Contact details
            * Regulatory information
            * Registration date
            * Current status (active, suspended, etc.)

            ### Performance Considerations
            This endpoint may return a large amount of data if many TPPs are registered.
            Consider implementing pagination if the number of TPPs grows significantly.

            ### Security Considerations
            Access to this endpoint should be restricted to authorized personnel only.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers found",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDThirdPartyProviderDTO> getAllProviders() {
        log.debug("REST request to get all providers");
        return thirdPartyProviderService.getAllProviders();
    }

    @PutMapping(value = "/{providerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update provider", 
        description = """
            ## Update a Third Party Provider

            This endpoint allows for updating the information of an existing Third Party Provider (TPP).

            ### Description
            This operation enables authorized users to modify the details of a registered TPP.
            It's used when TPP information changes or needs correction.

            ### Path Parameters
            * `providerId` - The unique identifier of the TPP to update

            ### Request Body
            The request must include the updated TPP information:
            * TPP name and identification details
            * Contact information
            * Regulatory information
            * Any other fields that need updating

            ### Processing
            The system will:
            1. Validate the provided TPP ID
            2. Check if the TPP exists in the system
            3. Verify that the requester has permission to update the TPP
            4. Validate the updated information
            5. Apply the changes to the TPP record

            ### Response
            Returns the updated TPP profile with all changes applied.

            ### Validation Rules
            * The TPP must exist in the system
            * Required fields cannot be empty
            * Regulatory information must be valid

            ### Security Considerations
            * Only authorized administrators should be able to update TPP information
            * All changes are logged for audit purposes
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider updated successfully",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> updateProvider(
            @Parameter(description = "ID of the provider", required = true)
            @PathVariable UUID providerId,
            @Valid @RequestBody PSDThirdPartyProviderDTO providerUpdate) {
        log.debug("REST request to update provider: {}", providerId);
        return thirdPartyProviderService.updateProvider(providerId, providerUpdate);
    }

    @PostMapping(value = "/{providerId}/suspend", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Suspend provider", 
        description = """
            ## Suspend a Third Party Provider

            This endpoint temporarily suspends a Third Party Provider's (TPP) access to the system.

            ### Description
            This operation allows authorized administrators to temporarily disable a TPP's ability 
            to access the APIs. This is typically used when there are security concerns, regulatory 
            issues, or at the request of the TPP itself.

            ### Path Parameters
            * `providerId` - The unique identifier of the TPP to suspend

            ### Processing
            The system will:
            1. Validate the provided TPP ID
            2. Check if the TPP exists in the system
            3. Verify that the requester has permission to suspend TPPs
            4. Change the TPP's status to "SUSPENDED"
            5. Invalidate any active sessions for the TPP

            ### Response
            Returns the updated TPP profile with status changed to "SUSPENDED".

            ### Business Rules
            * A suspended TPP cannot access any APIs
            * All existing tokens for the TPP are invalidated
            * The suspension can be reversed using the activate endpoint

            ### Security Considerations
            * Only authorized administrators should be able to suspend TPPs
            * The action is logged for audit purposes
            * Notification may be sent to the TPP and relevant regulatory authorities
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider suspended successfully",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> suspendProvider(
            @Parameter(description = "ID of the provider", required = true)
            @PathVariable UUID providerId) {
        log.debug("REST request to suspend provider: {}", providerId);
        return thirdPartyProviderService.suspendProvider(providerId);
    }

    @PostMapping(value = "/{providerId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Activate provider", 
        description = """
            ## Activate a Third Party Provider

            This endpoint activates or reactivates a Third Party Provider's (TPP) access to the system.

            ### Description
            This operation allows authorized administrators to enable a TPP's ability to access the APIs.
            This is typically used when onboarding a new TPP, or reactivating a previously suspended TPP
            after resolving any issues that led to the suspension.

            ### Path Parameters
            * `providerId` - The unique identifier of the TPP to activate

            ### Processing
            The system will:
            1. Validate the provided TPP ID
            2. Check if the TPP exists in the system
            3. Verify that the requester has permission to activate TPPs
            4. Change the TPP's status to "ACTIVE"
            5. Enable API access for the TPP

            ### Response
            Returns the updated TPP profile with status changed to "ACTIVE".

            ### Business Rules
            * An activated TPP can access APIs according to their permissions
            * The TPP must have valid certificates and regulatory approvals
            * If the TPP was previously suspended, a review process may be required before activation

            ### Security Considerations
            * Only authorized administrators should be able to activate TPPs
            * The action is logged for audit purposes
            * Activation should only proceed after all security and regulatory requirements are met
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider activated successfully",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> activateProvider(
            @Parameter(description = "ID of the provider", required = true)
            @PathVariable UUID providerId) {
        log.debug("REST request to activate provider: {}", providerId);
        return thirdPartyProviderService.activateProvider(providerId);
    }

    @DeleteMapping(value = "/{providerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Revoke provider", 
        description = """
            ## Revoke a Third Party Provider

            This endpoint permanently revokes a Third Party Provider's (TPP) access to the system.

            ### Description
            This operation allows authorized administrators to permanently disable a TPP's ability 
            to access the APIs. This is typically used when a TPP is no longer authorized to provide 
            services, has violated terms of service, or has ceased operations.

            ### Path Parameters
            * `providerId` - The unique identifier of the TPP to revoke

            ### Processing
            The system will:
            1. Validate the provided TPP ID
            2. Check if the TPP exists in the system
            3. Verify that the requester has permission to revoke TPPs
            4. Permanently revoke the TPP's access
            5. Invalidate all tokens and credentials
            6. Mark the TPP record as "REVOKED" in the database

            ### Response
            Returns a boolean value indicating successful revocation (true).

            ### Business Rules
            * A revoked TPP cannot access any APIs
            * All existing tokens and credentials for the TPP are permanently invalidated
            * Revocation is a permanent action and cannot be easily reversed
            * A revoked TPP would need to re-register to regain access

            ### Security Considerations
            * Only authorized administrators should be able to revoke TPPs
            * The action is logged for audit purposes
            * Notification should be sent to the TPP and relevant regulatory authorities
            * All customer consents associated with the TPP should be reviewed
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider revoked successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<Boolean> revokeProvider(
            @Parameter(description = "ID of the provider", required = true)
            @PathVariable UUID providerId) {
        log.debug("REST request to revoke provider: {}", providerId);
        return thirdPartyProviderService.revokeProvider(providerId);
    }

    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Validate API key", 
        description = """
            ## Validate a Third Party Provider's API Key

            This endpoint validates the authenticity and status of a Third Party Provider's (TPP) API key.

            ### Description
            This operation verifies that an API key belongs to a registered TPP and that the TPP 
            is currently active in the system. This endpoint is typically used by API gateways and 
            authentication services to validate incoming requests from TPPs.

            ### Request Headers
            * `X-API-KEY` - The API key to validate

            ### Processing
            The system will:
            1. Extract the API key from the request header
            2. Look up the TPP associated with the API key
            3. Verify that the TPP exists and is in an active state
            4. Check that the API key has not expired or been revoked

            ### Response
            Returns the TPP profile associated with the API key if validation is successful.

            ### Error Scenarios
            * If the API key is not found, returns a 401 Unauthorized response
            * If the TPP is suspended or revoked, returns a 401 Unauthorized response
            * If the API key has expired, returns a 401 Unauthorized response

            ### Security Considerations
            * This endpoint should be rate-limited to prevent brute force attacks
            * Failed validation attempts should be logged for security monitoring
            * Consider implementing additional security measures like IP whitelisting

            ### Performance Considerations
            * Results of validation should be cached for a short period to improve performance
            * The validation process should be optimized for low latency
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key validated successfully",
                    content = @Content(schema = @Schema(implementation = PSDThirdPartyProviderDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid API key"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDThirdPartyProviderDTO> validateApiKey(
            @Parameter(description = "API key to validate", required = true)
            @RequestHeader("X-API-KEY") String apiKey) {
        log.debug("REST request to validate API key");
        return thirdPartyProviderService.validateApiKey(apiKey);
    }
}
