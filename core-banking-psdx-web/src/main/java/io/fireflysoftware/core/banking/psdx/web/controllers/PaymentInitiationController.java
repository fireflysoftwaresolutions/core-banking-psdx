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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.PaymentInitiationService;
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
 * REST controller for payment initiation services.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Initiation Services", description = "APIs for payment initiation services according to PSD2/PSD3 regulations")
public class PaymentInitiationController {

    private final PaymentInitiationService paymentInitiationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Initiate payment", 
        description = """
            ## Initiate a New Payment

            This endpoint initiates a new payment transaction based on the provided payment details.

            ### Description
            This operation is a core Payment Initiation Service (PIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to initiate payments on behalf of customers
            after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this payment initiation

            ### Request Body
            The request must include payment details:
            * Debtor account information
            * Creditor account information
            * Payment amount and currency
            * Payment type (e.g., single, recurring, bulk)
            * Execution date
            * Remittance information
            * End-to-end identifier (if applicable)

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers payment initiation
            3. Verify that the payment details are valid and complete
            4. Check for sufficient funds (preliminary check)
            5. Create a payment record with status "RECEIVED"
            6. Initiate the Strong Customer Authentication (SCA) process if required

            ### Response
            Returns a payment object containing:
            * Payment identifier
            * Current status (initially "RECEIVED")
            * Creation timestamp
            * All payment details as provided in the request
            * Links to related resources (e.g., status check, authorization)

            ### Payment Lifecycle
            1. **RECEIVED** - Initial state after payment is created
            2. **PENDING** - Payment is pending authorization
            3. **AUTHORIZED** - Payment has been authorized but not yet executed
            4. **EXECUTED** - Payment has been successfully executed
            5. **REJECTED** - Payment has been rejected
            6. **CANCELLED** - Payment has been cancelled

            ### Regulatory Compliance
            * This endpoint implements the Payment Initiation Service as defined in PSD2/PSD3
            * Strong Customer Authentication (SCA) is required for payment authorization
            * Access is logged for regulatory reporting and audit purposes

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Payment details are validated for security and compliance
            * Anti-fraud checks may be performed
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PSDPaymentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDPaymentDTO> initiatePayment(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Valid @RequestBody PSDPaymentInitiationRequestDTO paymentRequest) {
        log.debug("REST request to initiate payment with consent ID: {}", consentId);
        return paymentInitiationService.initiatePayment(consentId, paymentRequest);
    }

    @GetMapping(value = "/{paymentId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get payment status", 
        description = """
            ## Retrieve Payment Status

            This endpoint retrieves the current status of a specific payment.

            ### Description
            This operation is part of the Payment Initiation Service (PIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to check the status of a payment that was previously
            initiated through the API.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes access to this payment

            ### Path Parameters
            * `paymentId` - The unique identifier of the payment whose status is being retrieved

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers the specified payment
            3. Verify that the payment exists and is associated with the consent
            4. Retrieve the current status of the payment

            ### Response
            Returns a payment status object containing:
            * Payment identifier
            * Current status (e.g., "RECEIVED", "PENDING", "AUTHORIZED", "EXECUTED", "REJECTED", "CANCELLED")
            * Status timestamp (when the status was last updated)
            * Status reason (if applicable, especially for rejected payments)
            * Additional status details (if available)

            ### Status Definitions
            * **RECEIVED** - Payment has been received but not yet processed
            * **PENDING** - Payment is awaiting authorization or processing
            * **AUTHORIZED** - Payment has been authorized but not yet executed
            * **EXECUTED** - Payment has been successfully executed
            * **REJECTED** - Payment has been rejected (with reason)
            * **CANCELLED** - Payment has been cancelled by the user or system

            ### Error Scenarios
            * If the payment doesn't exist, returns a 404 Not Found response
            * If the payment exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response

            ### Regulatory Compliance
            * This endpoint implements the Payment Status Check as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Status checks may be rate-limited to prevent excessive polling
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status found",
                    content = @Content(schema = @Schema(implementation = PSDPaymentStatusDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDPaymentStatusDTO> getPaymentStatus(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the payment", required = true)
            @PathVariable UUID paymentId) {
        log.debug("REST request to get status for payment: {} with consent ID: {}", paymentId, consentId);
        return paymentInitiationService.getPaymentStatus(consentId, paymentId);
    }

    @GetMapping(value = "/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get payment", 
        description = """
            ## Retrieve Payment Details

            This endpoint retrieves detailed information about a specific payment.

            ### Description
            This operation is part of the Payment Initiation Service (PIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to retrieve complete details of a payment that was
            previously initiated through the API.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes access to this payment

            ### Path Parameters
            * `paymentId` - The unique identifier of the payment to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers the specified payment
            3. Verify that the payment exists and is associated with the consent
            4. Retrieve the complete payment information

            ### Response
            Returns a payment object containing:
            * Payment identifier
            * Current status
            * Creation and last updated timestamps
            * Debtor account information
            * Creditor account information
            * Payment amount and currency
            * Payment type (e.g., single, recurring, bulk)
            * Execution date
            * Remittance information
            * End-to-end identifier (if applicable)
            * Status history (if available)
            * Links to related resources (e.g., status check, authorization)

            ### Error Scenarios
            * If the payment doesn't exist, returns a 404 Not Found response
            * If the payment exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response

            ### Data Sensitivity
            * Certain payment details may be masked or filtered based on the consent scope
            * Account numbers may be partially masked for security
            * Sensitive authentication data is never returned

            ### Regulatory Compliance
            * This endpoint implements the Payment Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Access to payment details is strictly controlled based on consent
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PSDPaymentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDPaymentDTO> getPayment(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the payment", required = true)
            @PathVariable UUID paymentId) {
        log.debug("REST request to get payment: {} with consent ID: {}", paymentId, consentId);
        return paymentInitiationService.getPayment(consentId, paymentId);
    }

    @DeleteMapping(value = "/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Cancel payment", 
        description = """
            ## Cancel a Payment

            This endpoint cancels a payment that has not yet been executed.

            ### Description
            This operation is part of the Payment Initiation Service (PIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to cancel a payment that was previously initiated
            but has not yet been fully processed or executed.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes access to this payment

            ### Path Parameters
            * `paymentId` - The unique identifier of the payment to cancel

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers the specified payment
            3. Verify that the payment exists and is associated with the consent
            4. Check if the payment is in a state that allows cancellation
            5. Update the payment status to "CANCELLED"
            6. Record the cancellation details

            ### Response
            Returns the updated payment object with status changed to "CANCELLED".

            ### Cancellation Rules
            * Only payments in certain states can be cancelled:
              * RECEIVED - Can be cancelled
              * PENDING - Can be cancelled
              * AUTHORIZED - May be cancellable depending on the payment system
              * EXECUTED - Cannot be cancelled (returns an error)
              * REJECTED - Already terminated (returns an error)
              * CANCELLED - Already cancelled (returns an error)
            * Cancellation may require customer authentication depending on the payment type and status
            * Some payment types may have time limitations for cancellation

            ### Error Scenarios
            * If the payment doesn't exist, returns a 404 Not Found response
            * If the payment exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the payment cannot be cancelled (e.g., already executed), returns a 400 Bad Request response

            ### Regulatory Compliance
            * This endpoint implements the Payment Cancellation Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Cancellation may be subject to specific regulatory requirements

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Cancellation may require additional authentication for security
            * All cancellation attempts are logged for audit purposes
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = PSDPaymentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDPaymentDTO> cancelPayment(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the payment", required = true)
            @PathVariable UUID paymentId) {
        log.debug("REST request to cancel payment: {} with consent ID: {}", paymentId, consentId);
        return paymentInitiationService.cancelPayment(consentId, paymentId);
    }

    @PostMapping(value = "/{paymentId}/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Authorize payment", 
        description = """
            ## Authorize a Payment

            This endpoint authorizes a payment using Strong Customer Authentication (SCA).

            ### Description
            This operation is part of the Payment Initiation Service (PIS) under PSD2/PSD3 regulations.
            It completes the authorization process for a payment by applying the authentication code
            obtained through the Strong Customer Authentication (SCA) process.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes access to this payment

            ### Path Parameters
            * `paymentId` - The unique identifier of the payment to authorize

            ### Request Body
            * `authorizationCode` - The authorization code obtained from the SCA process

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers the specified payment
            3. Verify that the payment exists and is associated with the consent
            4. Check if the payment is in a state that allows authorization
            5. Validate the authorization code against the SCA challenge
            6. Update the payment status to "AUTHORIZED"
            7. Initiate the payment execution process

            ### Response
            Returns the updated payment object with status changed to "AUTHORIZED".

            ### Authorization Flow
            1. Payment is initiated (status: RECEIVED)
            2. SCA is triggered (status: PENDING)
            3. Customer completes authentication and receives an authorization code
            4. TPP submits the authorization code to this endpoint
            5. Payment is authorized (status: AUTHORIZED)
            6. Payment is executed (status will change to EXECUTED asynchronously)

            ### SCA Requirements
            * Strong Customer Authentication is mandatory for payment authorization under PSD2/PSD3
            * SCA typically involves two-factor authentication (2FA)
            * The authorization code has a limited validity period
            * Multiple failed authorization attempts may lock the payment

            ### Error Scenarios
            * If the payment doesn't exist, returns a 404 Not Found response
            * If the payment exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the authorization code is invalid or expired, returns a 401 Unauthorized response
            * If the payment cannot be authorized (e.g., already executed), returns a 400 Bad Request response

            ### Regulatory Compliance
            * This endpoint implements the Payment Authorization Service as defined in PSD2/PSD3
            * Strong Customer Authentication is a regulatory requirement
            * Access is logged for regulatory reporting and audit purposes

            ### Security Considerations
            * All requests must include a valid consent ID
            * Authorization codes are single-use and time-limited
            * Failed authorization attempts are monitored for fraud prevention
            * All authorization attempts are logged for audit purposes
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment authorized successfully",
                    content = @Content(schema = @Schema(implementation = PSDPaymentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDPaymentDTO> authorizePayment(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the payment", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "Authorization code from SCA", required = true)
            @RequestBody String authorizationCode) {
        log.debug("REST request to authorize payment: {} with consent ID: {}", paymentId, consentId);
        return paymentInitiationService.authorizePayment(consentId, paymentId, authorizationCode);
    }
}
