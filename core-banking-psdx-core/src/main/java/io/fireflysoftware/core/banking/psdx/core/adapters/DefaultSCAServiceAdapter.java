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


package io.fireflysoftware.core.banking.psdx.core.adapters;

import io.fireflysoftware.core.banking.psdx.core.ports.SCAServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationResponseDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Default adapter for the SCA service.
 * This is a simplified implementation for demonstration purposes.
 * In a real application, this would be replaced with a client for the SCA service.
 */
@Component
@Slf4j
public class DefaultSCAServiceAdapter implements SCAServicePort {

    @Value("${psdx.sca.required-for-all-payments:true}")
    private boolean scaRequiredForAllPayments;

    @Value("${psdx.sca.exemption-threshold-amount:30.00}")
    private double scaExemptionThresholdAmount;

    @Value("${psdx.sca.exemption-threshold-currency:EUR}")
    private String scaExemptionThresholdCurrency;

    /**
     * Initiate SCA for a customer.
     *
     * @param request The SCA authentication request
     * @return A Mono of PSDSCAAuthenticationResponseDTO
     */
    @Override
    public Mono<PSDSCAAuthenticationResponseDTO> initiateSCA(PSDSCAAuthenticationRequestDTO request) {
        log.debug("Initiating SCA for party ID: {}, resource: {}", request.getPartyId(), request.getResourceId());
        
        // In a real implementation, this would call the SCA service
        // For now, we'll just return a mock response
        return Mono.just(PSDSCAAuthenticationResponseDTO.builder()
                .challengeId("sca-" + System.currentTimeMillis())
                .method("SMS")
                .maskedTarget("+49 *** *** 789")
                .expiresIn(300)
                .additionalInfo("An SMS has been sent to your registered mobile number")
                .build());
    }

    /**
     * Validate SCA for a customer.
     *
     * @param request The SCA validation request
     * @return A Mono of PSDSCAValidationResponseDTO
     */
    @Override
    public Mono<PSDSCAValidationResponseDTO> validateSCA(PSDSCAValidationRequestDTO request) {
        log.debug("Validating SCA for challenge ID: {}", request.getChallengeId());
        
        // In a real implementation, this would call the SCA service
        // For now, we'll just return a mock response
        // For demonstration purposes, we'll accept any code that is "123456"
        if ("123456".equals(request.getAuthenticationCode())) {
            return Mono.just(PSDSCAValidationResponseDTO.builder()
                    .success(true)
                    .authenticationToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                    .expiresIn(3600)
                    .build());
        } else {
            return Mono.just(PSDSCAValidationResponseDTO.builder()
                    .success(false)
                    .errorMessage("Invalid authentication code")
                    .build());
        }
    }

    /**
     * Check if SCA is required for a payment.
     *
     * @param amount The payment amount
     * @param currency The payment currency
     * @return A Mono of Boolean indicating if SCA is required
     */
    @Override
    public Mono<Boolean> isSCARequired(Double amount, String currency) {
        log.debug("Checking if SCA is required for amount: {} {}", amount, currency);
        
        // If SCA is required for all payments, return true
        if (scaRequiredForAllPayments) {
            return Mono.just(true);
        }
        
        // If the amount is below the exemption threshold and the currency matches, return false
        if (amount != null && currency != null && 
                amount <= scaExemptionThresholdAmount && 
                scaExemptionThresholdCurrency.equals(currency)) {
            return Mono.just(false);
        }
        
        // Otherwise, return true
        return Mono.just(true);
    }
}
