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


package io.fireflysoftware.core.banking.psdx.core.services;

import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.FundsConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Implementation of the FundsConfirmationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundsConfirmationServiceImpl implements FundsConfirmationService {

    private final AccountServicePort accountServicePort;
    private final ConsentService consentService;

    @Override
    public Mono<PSDFundsConfirmationDTO> confirmFunds(UUID consentId, PSDFundsConfirmationDTO fundsConfirmationRequest) {
        log.debug("Confirming funds using consent ID: {}", consentId);

        return consentService.validateConsent(consentId, "FUNDS_CONFIRMATION", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    // In a real implementation, this would check the account balance
                    // For now, we'll just return a mock response
                    fundsConfirmationRequest.setFundsConfirmationId(UUID.randomUUID());
                    fundsConfirmationRequest.setConsentId(consentId);
                    fundsConfirmationRequest.setFundsAvailable(true);
                    fundsConfirmationRequest.setConfirmationDateTime(LocalDateTime.now());

                    return Mono.just(fundsConfirmationRequest)
                            .doOnSuccess(confirmation -> log.info("Funds confirmation created with ID: {}", confirmation.getFundsConfirmationId()));
                });
    }

    @Override
    public Mono<PSDFundsConfirmationDTO> getFundsConfirmation(UUID consentId, UUID fundsConfirmationId) {
        log.debug("Getting funds confirmation with ID: {} using consent ID: {}", fundsConfirmationId, consentId);

        return consentService.validateConsent(consentId, "FUNDS_CONFIRMATION", "READ")
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }

                    // In a real implementation, this would retrieve the funds confirmation from a repository
                    // For now, we'll just return a mock response
                    PSDFundsConfirmationDTO confirmation = new PSDFundsConfirmationDTO();
                    confirmation.setFundsConfirmationId(fundsConfirmationId);
                    confirmation.setConsentId(consentId);
                    confirmation.setFundsAvailable(true);
                    confirmation.setConfirmationDateTime(LocalDateTime.now().minusHours(1));

                    return Mono.just(confirmation)
                            .doOnSuccess(result -> log.debug("Retrieved funds confirmation with ID: {}", fundsConfirmationId));
                });
    }
}
