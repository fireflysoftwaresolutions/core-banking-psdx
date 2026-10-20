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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;



/**
 * REST controller for consent management.
 */
@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consent Management", description = "APIs for managing consents according to PSD2/PSD3 regulations")
public class ConsentController {

    private final ConsentService consentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new consent", 
        description = """
            ## Create a New Consent

            This endpoint creates a new consent record for a customer, allowing a Third Party Provider (TPP) to access their data.

            ### Description
            Consent is a fundamental concept in PSD2/PSD3 regulations, representing the customer's explicit permission 
            for a TPP to access their account information or initiate payments. This endpoint creates a new consent 
            record based on the customer's authorization.

            ### Request Body
            The request must include:
            * Customer identification (party ID)
            * TPP identification
            * Type of consent (account information, payment initiation, etc.)
            * Scope of access (which accounts, what information)
            * Validity period (start and end dates)

            ### Processing
            The system will:
            1. Validate the request data
            2. Verify that the TPP is authorized and active
            3. Check that the customer exists
            4. Create a new consent record with status "RECEIVED"
            5. Generate a unique consent identifier

            ### Response
            Returns the created consent object with:
            * Unique consent ID
            * Current status (initially "RECEIVED")
            * Creation timestamp
            * All consent details as provided in the request

            ### Business Rules
            * A consent is initially created with status "RECEIVED"
            * The consent requires customer authentication before becoming active
            * Consents have a limited validity period as specified in the request
            * Different consent types may have different validation rules

            ### Security Considerations
            * Consent creation should be logged for audit purposes
            * The TPP must be authenticated and authorized to create consents
            * Customer verification will be required in a subsequent step
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consent created successfully",
                    content = @Content(schema = @Schema(implementation = PSDConsentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDConsentDTO> createConsent(
            @Valid @RequestBody PSDConsentRequestDTO consentRequest) {
        log.debug("REST request to create consent: {}", consentRequest);
        return consentService.createConsent(consentRequest);
    }

    @GetMapping(value = "/{consentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get a consent", 
        description = """
            ## Retrieve a Specific Consent

            This endpoint retrieves detailed information about a specific consent by its unique identifier.

            ### Description
            This operation allows TPPs and authorized users to access the complete details of a consent record.
            It's used to check the status, scope, and validity of a consent that was previously created.

            ### Path Parameters
            * `consentId` - The unique identifier of the consent to retrieve

            ### Processing
            The system will:
            1. Validate the provided consent ID
            2. Check if the consent exists in the system
            3. Verify that the requester has permission to view the consent
               (either the TPP that created it or an authorized administrator)
            4. Retrieve the consent details

            ### Response
            Returns the complete consent record including:
            * Consent ID and status
            * Customer information
            * TPP information
            * Scope of access granted
            * Validity period
            * Creation and last updated timestamps
            * Authentication status

            ### Access Control
            * TPPs can only access consents they created
            * Customers can access their own consents
            * Administrators can access all consents

            ### Security Considerations
            * Access to consent information should be strictly controlled
            * All access attempts should be logged for audit purposes
            * Sensitive customer information should be appropriately protected
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consent found",
                    content = @Content(schema = @Schema(implementation = PSDConsentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consent not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDConsentDTO> getConsent(
            @Parameter(description = "ID of the consent", required = true)
            @PathVariable UUID consentId) {
        log.debug("REST request to get consent: {}", consentId);
        return consentService.getConsent(consentId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get consents for a customer", 
        description = """
            ## Retrieve All Consents for a Customer

            This endpoint retrieves all consent records associated with a specific customer.

            ### Description
            This operation allows authorized users to view all consents that a customer has granted 
            to various TPPs. This is useful for customer support, consent management, and regulatory 
            reporting purposes.

            ### Query Parameters
            * `partyId` - The unique identifier of the customer whose consents are being retrieved

            ### Processing
            The system will:
            1. Validate the provided party ID
            2. Verify that the requester has permission to view the customer's consents
            3. Retrieve all consent records associated with the customer
            4. Filter the results based on the requester's access rights

            ### Response
            Returns an array of consent records, each containing:
            * Consent ID and status
            * TPP information
            * Scope of access granted
            * Validity period
            * Creation and last updated timestamps
            * Authentication status

            ### Access Control
            * TPPs can only access consents they created for the specified customer
            * Customers can access all their own consents
            * Administrators can access all consents for any customer

            ### Performance Considerations
            * For customers with many consents, consider implementing pagination
            * Results may be filtered based on consent status or TPP if needed

            ### Security Considerations
            * Access to customer consent information should be strictly controlled
            * All access attempts should be logged for audit purposes
            * Consider implementing additional filtering options to limit data exposure
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consents found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PSDConsentDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDConsentDTO> getConsentsForCustomer(
            @Parameter(description = "ID of the customer", required = true)
            @RequestParam UUID partyId) {
        log.debug("REST request to get consents for customer: {}", partyId);
        return consentService.getConsentsForCustomer(partyId);
    }

    @PutMapping(value = "/{consentId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update consent status", 
        description = """
            ## Update the Status of a Consent

            This endpoint changes the status of an existing consent.

            ### Description
            This operation allows for updating the status of a consent throughout its lifecycle.
            Consent status transitions are strictly controlled according to PSD2/PSD3 regulations
            and business rules.

            ### Path Parameters
            * `consentId` - The unique identifier of the consent to update

            ### Request Body
            The request must include:
            * New status value (e.g., "RECEIVED", "VALID", "REJECTED", "EXPIRED", "REVOKED")
            * Optional status reason or additional information

            ### Processing
            The system will:
            1. Validate the provided consent ID
            2. Check if the consent exists in the system
            3. Verify that the requester has permission to update the consent status
            4. Validate that the requested status transition is allowed
            5. Update the consent status
            6. Record the status change in the audit log

            ### Response
            Returns the updated consent record with the new status.

            ### Status Transition Rules
            * Only certain status transitions are allowed (e.g., "RECEIVED" → "VALID", "VALID" → "REVOKED")
            * Some status changes require specific authentication or authorization
            * Certain status changes may be irreversible (e.g., "REVOKED" cannot be changed)
            * Status changes may trigger notifications to the customer and/or TPP

            ### Regulatory Implications
            * Status changes must comply with PSD2/PSD3 requirements
            * All status changes must be properly documented and auditable
            * Certain status changes may require reporting to regulatory authorities

            ### Security Considerations
            * Status changes should be strictly controlled
            * All status changes must be logged for audit purposes
            * Status change authorization should be verified
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consent status updated",
                    content = @Content(schema = @Schema(implementation = PSDConsentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consent not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDConsentDTO> updateConsentStatus(
            @Parameter(description = "ID of the consent", required = true)
            @PathVariable UUID consentId,
            @Valid @RequestBody PSDConsentStatusDTO statusUpdate) {
        log.debug("REST request to update consent status: {} to {}", consentId, statusUpdate.getStatus());
        return consentService.updateConsentStatus(consentId, statusUpdate);
    }

    @DeleteMapping(value = "/{consentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Revoke a consent", 
        description = """
            ## Revoke a Customer Consent

            This endpoint revokes an existing consent, immediately terminating a TPP's access to customer data.

            ### Description
            This operation allows customers or authorized administrators to revoke a previously granted consent.
            Revocation is an important right under PSD2/PSD3 regulations, allowing customers to withdraw 
            their permission for data access at any time.

            ### Path Parameters
            * `consentId` - The unique identifier of the consent to revoke

            ### Processing
            The system will:
            1. Validate the provided consent ID
            2. Check if the consent exists in the system
            3. Verify that the requester has permission to revoke the consent
               (either the customer who granted it or an authorized administrator)
            4. Change the consent status to "REVOKED"
            5. Invalidate any access tokens associated with the consent
            6. Record the revocation in the audit log
            7. Notify the TPP of the revocation

            ### Response
            Returns the updated consent record with status changed to "REVOKED".

            ### Business Rules
            * A revoked consent cannot be reactivated
            * All access based on the revoked consent is immediately terminated
            * The TPP must be notified of the revocation
            * Revocation can be initiated by the customer or by an administrator
            * Revocation reasons should be recorded for audit purposes

            ### Regulatory Implications
            * Customers have the right to revoke consent at any time under PSD2/PSD3
            * Revocation must be processed without undue delay
            * The revocation process must be simple and straightforward

            ### Security Considerations
            * Revocation should be strictly controlled but easily accessible to customers
            * All revocations must be logged for audit purposes
            * Systems should ensure that access is immediately terminated upon revocation
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consent revoked",
                    content = @Content(schema = @Schema(implementation = PSDConsentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consent not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDConsentDTO> revokeConsent(
            @Parameter(description = "ID of the consent", required = true)
            @PathVariable UUID consentId) {
        log.debug("REST request to revoke consent: {}", consentId);
        return consentService.revokeConsent(consentId);
    }
}
