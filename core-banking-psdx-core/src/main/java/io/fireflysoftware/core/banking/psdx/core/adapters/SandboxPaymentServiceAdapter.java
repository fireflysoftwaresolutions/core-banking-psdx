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

import io.fireflysoftware.core.banking.psdx.core.ports.PaymentServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * In-memory completed payment. The same request or payment id always returns the same record.
 */
public class SandboxPaymentServiceAdapter implements PaymentServicePort {

    private static final String COMPLETED = "ACSC";

    @Override
    public Mono<PSDPaymentDTO> initiatePayment(PSDPaymentInitiationRequestDTO paymentRequest) {
        return Mono.just(fromRequest(paymentRequest));
    }

    @Override
    public Mono<PSDPaymentStatusDTO> getPaymentStatus(UUID paymentId) {
        return Mono.just(PSDPaymentStatusDTO.builder()
                .paymentId(paymentId)
                .transactionStatus(COMPLETED)
                .status(COMPLETED)
                .fundsAvailable(Boolean.TRUE)
                .psuMessage("Sandbox payment completed")
                .build());
    }

    @Override
    public Mono<PSDPaymentDTO> getPayment(UUID paymentId) {
        return Mono.just(sample(paymentId, COMPLETED));
    }

    @Override
    public Mono<Boolean> cancelPayment(UUID paymentId) {
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public Mono<PSDPaymentDTO> authorizePayment(UUID paymentId, String authorizationCode) {
        return Mono.just(sample(paymentId, COMPLETED));
    }

    private static PSDPaymentDTO fromRequest(PSDPaymentInitiationRequestDTO request) {
        String material = request == null
                ? "absent"
                : String.valueOf(request.getEndToEndIdentification())
                + ":" + String.valueOf(request.getCreditorName())
                + ":" + amountText(request.getInstructedAmount());
        UUID paymentId = SandboxSamples.id("payment", material);
        PSDPaymentDTO payment = sample(paymentId, COMPLETED);
        if (request == null) {
            return payment;
        }
        payment.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "sepa-credit-transfers");
        payment.setEndToEndIdentification(request.getEndToEndIdentification());
        payment.setCreditorName(request.getCreditorName());
        payment.setRemittanceInformationUnstructured(request.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(request.getRequestedExecutionDate());
        payment.setDebtorAccount(reference(request.getDebtorAccount()));
        payment.setCreditorAccount(reference(request.getCreditorAccount()));
        if (request.getInstructedAmount() != null) {
            payment.setInstructedAmount(PSDPaymentDTO.PSDAmountDTO.builder()
                    .currency(request.getInstructedAmount().getCurrency())
                    .amount(request.getInstructedAmount().getAmount())
                    .build());
        }
        return payment;
    }

    private static PSDPaymentDTO sample(UUID paymentId, String status) {
        return PSDPaymentDTO.builder()
                .paymentId(paymentId)
                .consentId(SandboxSamples.id("payment-consent", SandboxSamples.text(paymentId)))
                .paymentType("sepa-credit-transfers")
                .transactionStatus(status)
                .creditorName("Sandbox creditor")
                .instructedAmount(PSDPaymentDTO.PSDAmountDTO.builder()
                        .currency(SandboxSamples.SAMPLE_CURRENCY)
                        .amount(new BigDecimal("100.00"))
                        .build())
                .debtorAccount(account(SandboxSamples.SAMPLE_IBAN, SandboxSamples.SAMPLE_CURRENCY))
                .creditorAccount(account(SandboxSamples.SAMPLE_IBAN, SandboxSamples.SAMPLE_CURRENCY))
                .createdAt(SandboxSamples.SAMPLE_TIME)
                .updatedAt(SandboxSamples.SAMPLE_TIME)
                .build();
    }

    private static PSDPaymentDTO.PSDAccountReferenceDTO reference(
            PSDPaymentInitiationRequestDTO.PSDAccountReferenceDTO source) {
        if (source == null) {
            return account(SandboxSamples.SAMPLE_IBAN, SandboxSamples.SAMPLE_CURRENCY);
        }
        return PSDPaymentDTO.PSDAccountReferenceDTO.builder()
                .iban(source.getIban())
                .bban(source.getBban())
                .pan(source.getPan())
                .maskedPan(source.getMaskedPan())
                .msisdn(source.getMsisdn())
                .currency(source.getCurrency())
                .build();
    }

    private static PSDPaymentDTO.PSDAccountReferenceDTO account(String iban, String currency) {
        return PSDPaymentDTO.PSDAccountReferenceDTO.builder()
                .iban(iban)
                .currency(currency)
                .build();
    }

    private static String amountText(PSDPaymentInitiationRequestDTO.PSDAmountDTO amount) {
        if (amount == null || amount.getAmount() == null) {
            return "0";
        }
        return amount.getCurrency() + ":" + amount.getAmount().toPlainString();
    }
}
