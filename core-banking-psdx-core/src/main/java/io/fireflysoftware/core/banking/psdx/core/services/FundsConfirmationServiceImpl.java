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
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccountReferenceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAmountDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDBalanceDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDFundsConfirmationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import io.fireflysoftware.core.banking.psdx.interfaces.services.FundsConfirmationService;
import io.fireflysoftware.core.banking.psdx.models.entities.FundsConfirmation;
import io.fireflysoftware.core.banking.psdx.models.repositories.FundsConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Confirms funds from the account balances and stores the decision.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundsConfirmationServiceImpl implements FundsConfirmationService {

    private final AccountServicePort accountServicePort;
    private final ConsentService consentService;
    private final FundsConfirmationRepository fundsConfirmationRepository;

    @Override
    public Mono<PSDFundsConfirmationDTO> confirmFunds(UUID consentId, PSDFundsConfirmationDTO fundsConfirmationRequest) {
        log.debug("Confirming funds using consent ID: {}", consentId);

        return consentService.validateConsent(consentId, "FUNDS_CONFIRMATION", "READ")
                .flatMap(isValid -> {
                    if (!Boolean.TRUE.equals(isValid)) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }
                    String reference = reference(fundsConfirmationRequest.getAccount());
                    return accountServicePort.balancesForReference(reference)
                            .collectList()
                            .flatMap(balances -> {
                                boolean available = covers(balances.stream().map(PSDBalanceDTO::getBalanceAmount).toList(),
                                        fundsConfirmationRequest.getInstructedAmount());
                                FundsConfirmation stored = FundsConfirmation.builder()
                                        .id(UUID.randomUUID())
                                        .consentId(consentId)
                                        .accountReference(reference)
                                        .amount(amount(fundsConfirmationRequest.getInstructedAmount()))
                                        .currency(currency(fundsConfirmationRequest.getInstructedAmount()))
                                        .creditorName(fundsConfirmationRequest.getCreditorName())
                                        .creditorAccount(reference(fundsConfirmationRequest.getCreditorAccount()))
                                        .cardNumber(fundsConfirmationRequest.getCardNumber())
                                        .psuName(fundsConfirmationRequest.getPsuName())
                                        .fundsAvailable(available)
                                        .confirmationDateTime(LocalDateTime.now().withNano(0))
                                        .build();
                                return fundsConfirmationRepository.save(stored).map(this::toDto);
                            });
                });
    }

    @Override
    public Mono<PSDFundsConfirmationDTO> getFundsConfirmation(UUID consentId, UUID fundsConfirmationId) {
        log.debug("Getting funds confirmation with ID: {} using consent ID: {}", fundsConfirmationId, consentId);

        return consentService.validateConsent(consentId, "FUNDS_CONFIRMATION", "READ")
                .flatMap(isValid -> {
                    if (!Boolean.TRUE.equals(isValid)) {
                        return Mono.error(new IllegalArgumentException("Invalid or expired consent"));
                    }
                    return fundsConfirmationRepository.findById(fundsConfirmationId)
                            .filter(stored -> consentId.equals(stored.getConsentId()))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Funds confirmation not found")))
                            .map(this::toDto);
                });
    }

    private PSDFundsConfirmationDTO toDto(FundsConfirmation stored) {
        PSDFundsConfirmationDTO dto = new PSDFundsConfirmationDTO();
        dto.setFundsConfirmationId(stored.getId());
        dto.setConsentId(stored.getConsentId());
        dto.setFundsAvailable(stored.getFundsAvailable());
        dto.setConfirmationDateTime(stored.getConfirmationDateTime());
        dto.setCreditorName(stored.getCreditorName());
        dto.setCardNumber(stored.getCardNumber());
        dto.setPsuName(stored.getPsuName());
        if (stored.getAccountReference() != null || stored.getCurrency() != null) {
            dto.setAccount(PSDAccountReferenceDTO.builder()
                    .iban(stored.getAccountReference())
                    .currency(stored.getCurrency())
                    .build());
        }
        if (stored.getAmount() != null || stored.getCurrency() != null) {
            PSDAmountDTO amount = new PSDAmountDTO();
            amount.setAmount(stored.getAmount());
            amount.setCurrency(stored.getCurrency());
            dto.setInstructedAmount(amount);
        }
        return dto;
    }

    private static boolean covers(java.util.List<PSDBalanceDTO.PSDAmountDTO> balances, PSDAmountDTO instructed) {
        if (instructed == null || instructed.getAmount() == null || balances.isEmpty()) {
            return false;
        }
        BigDecimal available = BigDecimal.ZERO;
        for (PSDBalanceDTO.PSDAmountDTO balance : balances) {
            if (balance == null || balance.getAmount() == null) {
                continue;
            }
            if (instructed.getCurrency() != null && balance.getCurrency() != null
                    && !instructed.getCurrency().equalsIgnoreCase(balance.getCurrency())) {
                continue;
            }
            available = available.add(balance.getAmount());
        }
        return available.compareTo(instructed.getAmount()) >= 0;
    }

    private static String reference(PSDAccountReferenceDTO account) {
        if (account == null) {
            return null;
        }
        if (account.getIban() != null && !account.getIban().isBlank()) {
            return account.getIban();
        }
        if (account.getBban() != null && !account.getBban().isBlank()) {
            return account.getBban();
        }
        return account.getMaskedPan();
    }

    private static BigDecimal amount(PSDAmountDTO amount) {
        return amount == null ? null : amount.getAmount();
    }

    private static String currency(PSDAmountDTO amount) {
        return amount == null ? null : amount.getCurrency();
    }
}
