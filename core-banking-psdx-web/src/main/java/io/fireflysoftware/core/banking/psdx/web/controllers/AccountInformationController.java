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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDTransactionDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.AccountInformationService;
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
 * REST controller for account information services.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Information Services", description = "APIs for account information services according to PSD2/PSD3 regulations")
public class AccountInformationController {

    private final AccountInformationService accountInformationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get accounts", 
        description = """
            ## Retrieve All Customer Accounts

            This endpoint retrieves all accounts owned by a specific customer that are accessible under the provided consent.

            ### Description
            This operation is a core Account Information Service (AIS) under PSD2/PSD3 regulations. It allows 
            Third Party Providers (TPPs) to access a customer's account information after obtaining explicit 
            consent from the customer.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Query Parameters
            * `partyId` - The unique identifier of the customer whose accounts are being retrieved

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers account information access
            3. Verify that the consent is associated with the specified customer (partyId)
            4. Retrieve all accounts that the customer owns and that are covered by the consent
            5. Filter out any accounts that are not included in the consent's scope

            ### Response
            Returns an array of account objects, each containing:
            * Account identifier
            * Account type (e.g., current, savings, credit card)
            * Currency
            * Account name/description
            * Status
            * Basic account details

            ### Access Control
            * Access is only granted if a valid consent exists
            * Only accounts specified in the consent scope are returned
            * The TPP must be authenticated and authorized

            ### Regulatory Compliance
            * This endpoint implements the Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Data minimization principles are applied (only necessary data is returned)

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Access is monitored for suspicious patterns
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts found",
                    content = @Content(schema = @Schema(implementation = PSDAccountDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDAccountDTO> getAccounts(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the customer", required = true)
            @RequestParam UUID partyId) {
        log.debug("REST request to get accounts for party ID: {} with consent ID: {}", partyId, consentId);
        return accountInformationService.getAccounts(consentId, partyId);
    }

    @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get account", 
        description = """
            ## Retrieve a Specific Account

            This endpoint retrieves detailed information about a specific account identified by its unique ID.

            ### Description
            This operation is part of the Account Information Service (AIS) under PSD2/PSD3 regulations. 
            It allows Third Party Providers (TPPs) to access detailed information about a specific 
            customer account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `accountId` - The unique identifier of the account to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers account information access
            3. Verify that the specified account is included in the consent's scope
            4. Retrieve the detailed account information
            5. Apply any filtering required by the consent's scope

            ### Response
            Returns a single account object containing:
            * Account identifier
            * Account type (e.g., current, savings, credit card)
            * Currency
            * Account name/description
            * Status
            * Opening date
            * Detailed account information
            * Account holder information (as permitted by consent)

            ### Error Scenarios
            * If the account doesn't exist, returns a 404 Not Found response
            * If the account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response

            ### Regulatory Compliance
            * This endpoint implements the Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Data minimization principles are applied (only necessary data is returned)

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Access is monitored for suspicious patterns
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = PSDAccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDAccountDTO> getAccount(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the account", required = true)
            @PathVariable UUID accountId) {
        log.debug("REST request to get account: {} with consent ID: {}", accountId, consentId);
        return accountInformationService.getAccount(consentId, accountId);
    }

    @GetMapping(value = "/{accountId}/balances", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get balances", 
        description = """
            ## Retrieve Account Balances

            This endpoint retrieves the various balance types for a specific account.

            ### Description
            This operation is part of the Account Information Service (AIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access balance information for a specific 
            customer account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `accountId` - The unique identifier of the account whose balances are being retrieved

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers balance information access
            3. Verify that the specified account is included in the consent's scope
            4. Retrieve the various balance types for the account

            ### Response
            Returns an array of balance objects, each containing:
            * Balance type (e.g., closing booked, expected, available)
            * Amount and currency
            * Reference date and time of the balance
            * Credit/debit indicator
            * Status

            ### Balance Types
            The response may include multiple balance types:
            * **Closing Booked**: End-of-day balance from the previous banking business day
            * **Expected**: Balance including all pending transactions
            * **Available**: Balance available for immediate use
            * **Interim Available**: Provisional available balance
            * **Forward Available**: Future available balance based on scheduled transactions

            ### Error Scenarios
            * If the account doesn't exist, returns a 404 Not Found response
            * If the account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include balance access, returns a 403 Forbidden response

            ### Regulatory Compliance
            * This endpoint implements the Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Balance information is considered sensitive financial data

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Access frequency may be limited to prevent excessive requests
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balances found",
                    content = @Content(schema = @Schema(implementation = PSDBalanceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDBalanceDTO> getBalances(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the account", required = true)
            @PathVariable UUID accountId) {
        log.debug("REST request to get balances for account: {} with consent ID: {}", accountId, consentId);
        return accountInformationService.getBalances(consentId, accountId);
    }

    @GetMapping(value = "/{accountId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get transactions", 
        description = """
            ## Retrieve Account Transactions

            This endpoint retrieves transaction history for a specific account within a specified date range.

            ### Description
            This operation is part of the Account Information Service (AIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access transaction history for a specific 
            customer account after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `accountId` - The unique identifier of the account whose transactions are being retrieved

            ### Query Parameters
            * `fromDate` - Start date of the transaction period (ISO format: YYYY-MM-DD)
            * `toDate` - End date of the transaction period (ISO format: YYYY-MM-DD)

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers transaction access
            3. Verify that the specified account is included in the consent's scope
            4. Validate the date range (e.g., ensure fromDate is not after toDate)
            5. Retrieve transactions that occurred within the specified date range
            6. Apply any filtering required by the consent's scope

            ### Response
            Returns an array of transaction objects, each containing:
            * Transaction identifier
            * Status (booked, pending)
            * Booking date and value date
            * Amount and currency
            * Creditor/debtor information (as permitted by consent)
            * Transaction type
            * Remittance information
            * Additional transaction details

            ### Date Range Limitations
            * The maximum date range may be limited (e.g., 90 days) to prevent excessive data retrieval
            * Historical data beyond a certain point may not be available through this API
            * Future-dated transactions may be included if they are already known

            ### Error Scenarios
            * If the account doesn't exist, returns a 404 Not Found response
            * If the account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include transaction access, returns a 403 Forbidden response
            * If the date range is invalid, returns a 400 Bad Request response

            ### Regulatory Compliance
            * This endpoint implements the Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Transaction data is considered sensitive financial data

            ### Performance Considerations
            * For large date ranges, consider implementing pagination
            * Response times may vary based on the number of transactions
            * Consider using more specific date ranges for better performance
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found",
                    content = @Content(schema = @Schema(implementation = PSDTransactionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<PSDTransactionDTO> getTransactions(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the account", required = true)
            @PathVariable UUID accountId,
            @Parameter(description = "Start date of the range", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date of the range", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.debug("REST request to get transactions for account: {} between {} and {} with consent ID: {}",
                accountId, fromDate, toDate, consentId);
        return accountInformationService.getTransactions(consentId, accountId, fromDate, toDate);
    }

    @GetMapping(value = "/{accountId}/transactions/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get transaction", 
        description = """
            ## Retrieve a Specific Transaction

            This endpoint retrieves detailed information about a specific transaction identified by its unique ID.

            ### Description
            This operation is part of the Account Information Service (AIS) under PSD2/PSD3 regulations.
            It allows Third Party Providers (TPPs) to access detailed information about a specific 
            transaction after obtaining explicit consent.

            ### Request Headers
            * `X-Consent-ID` - The unique identifier of the consent that authorizes this access

            ### Path Parameters
            * `accountId` - The unique identifier of the account that contains the transaction
            * `transactionId` - The unique identifier of the transaction to retrieve

            ### Processing
            The system will:
            1. Validate the consent ID provided in the header
            2. Check that the consent is valid, active, and covers transaction access
            3. Verify that the specified account is included in the consent's scope
            4. Retrieve the detailed transaction information
            5. Apply any filtering required by the consent's scope

            ### Response
            Returns a single transaction object containing:
            * Transaction identifier
            * Status (booked, pending)
            * Booking date and value date
            * Amount and currency
            * Creditor/debtor information (as permitted by consent)
            * Transaction type
            * Remittance information
            * Additional transaction details
            * Related references (e.g., end-to-end ID, mandate ID)
            * Purpose code
            * Bank transaction code

            ### Error Scenarios
            * If the account doesn't exist, returns a 404 Not Found response
            * If the transaction doesn't exist, returns a 404 Not Found response
            * If the account exists but is not covered by the consent, returns a 403 Forbidden response
            * If the consent is invalid or expired, returns a 401 Unauthorized response
            * If the consent doesn't include transaction access, returns a 403 Forbidden response

            ### Regulatory Compliance
            * This endpoint implements the Account Information Service as defined in PSD2/PSD3
            * Access is logged for regulatory reporting and audit purposes
            * Transaction data is considered sensitive financial data

            ### Security Considerations
            * All requests must include a valid consent ID
            * Consent validation includes checking expiration and revocation status
            * Sensitive transaction details may be masked or filtered based on consent scope
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = PSDTransactionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PSDTransactionDTO> getTransaction(
            @Parameter(description = "ID of the consent", required = true)
            @RequestHeader("X-Consent-ID") UUID consentId,
            @Parameter(description = "ID of the account", required = true)
            @PathVariable UUID accountId,
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable UUID transactionId) {
        log.debug("REST request to get transaction: {} for account: {} with consent ID: {}",
                transactionId, accountId, consentId);
        return accountInformationService.getTransaction(consentId, accountId, transactionId);
    }
}
