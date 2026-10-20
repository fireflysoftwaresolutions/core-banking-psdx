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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDCardAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.CardAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for card account information.
 */
@RestController
@RequestMapping("/api/v1/card-accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Account Information", description = "APIs for card account information according to PSD2/PSD3 regulations")
public class CardAccountController {

    private final CardAccountService cardAccountService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get card accounts", 
        description = """
            ## Retrieve All Customer Card Accounts

            This endpoint retrieves all card accounts owned by a specific customer that are accessible under the provided consent.

            ### Description
            This operation is part of the Card Account Information Service under PSD2/PSD3 regulations. It allows 
            Third Party Providers (TPPs) to access a customer's card account information after obtaining explicit 
            consent from the customer.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Query Parameters
            * `partyId` - The unique identifier of the customer whose card accounts are being retrieved

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers card account information access
            3. Verify that the consent is associated with the specified customer (partyId)
            4. Retrieve all card accounts that the customer owns and that are covered by the consent
            5. Filter out any card accounts that are not included in the consent's scope

            ### Response
            Returns an array of card account objects, each containing:
            * Card account identifier
            * Card type (e.g., credit, debit, prepaid)
            * Card product name
            * Card number (masked)
            * Currency
            * Card holder name
            * Status
            * Expiry date
            * Credit limit (for credit cards)

            ### Access Control
            * Access is only granted if a valid consent exists
            * Only card accounts specified in the consent scope are returned
            * The TPP must be authenticated and authorized

            ### Regulatory Compliance
            * This endpoint implements the Card Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Data minimization principles are applied (only necessary data is returned)
            * Sensitive card details are masked according to security standards

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Full card numbers are never exposed through the API
            * Access is monitored for suspicious patterns
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card accounts found",
                    content = @Content(schema = @Schema(implementation = PSDCardAccountDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDCardAccountDTO> getCardAccounts(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the customer", required = true)
            @RequestParam UUID partyId) {
        log.debug("REST request to get card accounts for party ID: {} with consent ID: {}", partyId, consentId);
        return cardAccountService.getCardAccounts(consentId, partyId);
    }

    @GetMapping(value = "/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get card account", 
        description = """
            ## Retrieve a Specific Card Account

            This endpoint retrieves detailed information about a specific card account identified by its unique ID.

            ### Description
            This operation is part of the Card Account Information Service under PSD2/PSD3 regulations. 
            It allows Third Party Providers (TPPs) to access detailed information about a specific 
            customer card account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `cardId` - The unique identifier of the card account to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers card account information access
            3. Verify that the specified card account is included in the consent's scope
            4. Retrieve the detailed card account information
            5. Apply any filtering required by the consent's scope

            ### Response
            Returns a single card account object containing:
            * Card account identifier
            * Card type (e.g., credit, debit, prepaid)
            * Card product name
            * Card number (masked)
            * Currency
            * Card holder name
            * Status
            * Expiry date
            * Credit limit (for credit cards)
            * Detailed card account information

            ### Error Scenarios
            * If the card account doesn't exist, returns a 404 Not Found response
            * If the card account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response

            ### Regulatory Compliance
            * This endpoint implements the Card Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Data minimization principles are applied (only necessary data is returned)
            * Sensitive card details are masked according to security standards

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Full card numbers are never exposed through the API
            * Access is monitored for suspicious patterns
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card account found",
                    content = @Content(schema = @Schema(implementation = PSDCardAccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDCardAccountDTO> getCardAccount(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the card", required = true)
            @PathVariable UUID cardId) {
        log.debug("REST request to get card account: {} with consent ID: {}", cardId, consentId);
        return cardAccountService.getCardAccount(consentId, cardId);
    }

    @GetMapping(value = "/{cardId}/balances", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get card balances", 
        description = """
            ## Retrieve Card Account Balances

            This endpoint retrieves the various balance types for a specific card account.

            ### Description
            This operation is part of the Card Account Information Service under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access balance information for a specific 
            customer card account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `cardId` - The unique identifier of the card account whose balances are being retrieved

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers card balance information access
            3. Verify that the specified card account is included in the consent's scope
            4. Retrieve the various balance types for the card account

            ### Response
            Returns an array of balance objects, each containing:
            * Balance type (e.g., closing booked, expected, available)
            * Amount and currency
            * Reference date and time of the balance
            * Credit/debit indicator
            * Status

            ### Balance Types for Card Accounts
            The response may include multiple balance types:
            * **Outstanding** - Current outstanding balance on the card
            * **Available** - Available credit limit
            * **Credit Limit** - Total credit limit of the card
            * **Current** - Current balance including pending transactions
            * **Authorized** - Sum of authorized but not yet booked transactions

            ### Error Scenarios
            * If the card account doesn't exist, returns a 404 Not Found response
            * If the card account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include balance access, returns a 403 Forbidden response

            ### Regulatory Compliance
            * This endpoint implements the Card Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Balance information is considered sensitive financial data

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Access frequency may be limited to prevent excessive requests
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card balances found",
                    content = @Content(schema = @Schema(implementation = PSDBalanceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDBalanceDTO> getCardBalances(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the card", required = true)
            @PathVariable UUID cardId) {
        log.debug("REST request to get balances for card ID: {} with consent ID: {}", cardId, consentId);
        return cardAccountService.getCardBalances(consentId, cardId);
    }

    @GetMapping(value = "/{cardId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get card transactions", 
        description = """
            ## Retrieve Card Account Transactions

            This endpoint retrieves transaction history for a specific card account within a specified date range.

            ### Description
            This operation is part of the Card Account Information Service under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access transaction history for a specific 
            customer card account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `cardId` - The unique identifier of the card account whose transactions are being retrieved

            ### Query Parameters
            * `fromDate` - Start date of the transaction period (ISO format: YYYY-MM-DD)
            * `toDate` - End date of the transaction period (ISO format: YYYY-MM-DD)

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers card transaction access
            3. Verify that the specified card account is included in the consent's scope
            4. Validate the date range (e.g., ensure fromDate is not after toDate)
            5. Retrieve card transactions that occurred within the specified date range
            6. Apply any filtering required by the consent's scope

            ### Response
            Returns an array of transaction objects, each containing:
            * Transaction identifier
            * Status (booked, pending)
            * Booking date and value date
            * Amount and currency
            * Merchant information
            * Transaction type
            * Card acceptor details (e.g., merchant name, terminal ID)
            * Additional transaction details

            ### Card Transaction Specifics
            Card transactions may include additional information not present in regular account transactions:
            * Merchant category code (MCC)
            * Terminal ID
            * Point of sale details
            * Original transaction references for refunds
            * International transaction indicators

            ### Date Range Limitations
            * The maximum date range may be limited (e.g., 90 days) to prevent excessive data retrieval
            * Historical data beyond a certain point may not be available through this API

            ### Error Scenarios
            * If the card account doesn't exist, returns a 404 Not Found response
            * If the card account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include transaction access, returns a 403 Forbidden response
            * If the date range is invalid, returns a 400 Bad Request response

            ### Regulatory Compliance
            * This endpoint implements the Card Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Transaction data is considered sensitive financial data

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Sensitive merchant or cardholder data may be masked
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card transactions found",
                    content = @Content(schema = @Schema(implementation = PSDTransactionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDTransactionDTO> getCardTransactions(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the card", required = true)
            @PathVariable UUID cardId,
            @Parameter(description = "Start date of the range", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date of the range", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.debug("REST request to get transactions for card ID: {} between {} and {} with consent ID: {}",
                cardId, fromDate, toDate, consentId);
        return cardAccountService.getCardTransactions(consentId, cardId, fromDate, toDate);
    }

    @GetMapping(value = "/{cardId}/transactions/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get card transaction", 
        description = """
            ## Retrieve a Specific Card Transaction

            This endpoint retrieves detailed information about a specific card transaction identified by its unique ID.

            ### Description
            This operation is part of the Card Account Information Service under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access detailed information about a specific 
            card transaction after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `cardId` - The unique identifier of the card account that contains the transaction
            * `transactionId` - The unique identifier of the transaction to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers card transaction access
            3. Verify that the specified card account is included in the consent's scope
            4. Retrieve the detailed card transaction information
            5. Apply any filtering required by the consent's scope

            ### Response
            Returns a single transaction object containing:
            * Transaction identifier
            * Status (booked, pending)
            * Booking date and value date
            * Amount and currency
            * Merchant information
            * Transaction type
            * Card acceptor details (e.g., merchant name, terminal ID)
            * Merchant category code (MCC)
            * Terminal ID
            * Point of sale details
            * Original transaction references for refunds
            * International transaction indicators
            * Additional transaction details

            ### Error Scenarios
            * If the card account doesn't exist, returns a 404 Not Found response
            * If the transaction doesn't exist, returns a 404 Not Found response
            * If the card account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include transaction access, returns a 403 Forbidden response

            ### Regulatory Compliance
            * This endpoint implements the Card Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Transaction data is considered sensitive financial data

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Sensitive merchant or cardholder data may be masked
            * Full card numbers are never exposed in transaction details
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card transaction found",
                    content = @Content(schema = @Schema(implementation = PSDTransactionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Card transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDTransactionDTO> getCardTransaction(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the card", required = true)
            @PathVariable UUID cardId,
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable UUID transactionId) {
        log.debug("REST request to get transaction: {} for card ID: {} with consent ID: {}",
                transactionId, cardId, consentId);
        return cardAccountService.getCardTransaction(consentId, cardId, transactionId);
    }
}
