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

import io.fireflysoftware.core.banking.psdx.core.ports.PaymentServicePort;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentStatusDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentInitiationServiceImplTest {

    @Mock
    private PaymentServicePort paymentServicePort;

    @Mock
    private ConsentService consentService;

    @InjectMocks
    private PaymentInitiationServiceImpl paymentInitiationService;

    private final UUID CONSENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID PAYMENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
    private final String AUTHORIZATION_CODE = "123456";

    private PSDPaymentInitiationRequestDTO paymentRequest;
    private PSDPaymentDTO payment;
    private PSDPaymentStatusDTO paymentStatus;

    @BeforeEach
    void setUp() {
        // Setup test data
        PSDPaymentInitiationRequestDTO.PSDAccountReferenceDTO debtorAccount = new PSDPaymentInitiationRequestDTO.PSDAccountReferenceDTO();
        debtorAccount.setIban("DE89370400440532013000");

        PSDPaymentInitiationRequestDTO.PSDAccountReferenceDTO creditorAccount = new PSDPaymentInitiationRequestDTO.PSDAccountReferenceDTO();
        creditorAccount.setIban("FR1420041010050500013M02606");

        PSDPaymentInitiationRequestDTO.PSDAmountDTO amount = new PSDPaymentInitiationRequestDTO.PSDAmountDTO();
        amount.setCurrency("EUR");
        amount.setAmount(BigDecimal.valueOf(100.00));

        paymentRequest = new PSDPaymentInitiationRequestDTO();
        paymentRequest.setPaymentType("sepa-credit-transfers");
        paymentRequest.setDebtorAccount(debtorAccount);
        paymentRequest.setCreditorName("John Doe");
        paymentRequest.setCreditorAccount(creditorAccount);
        paymentRequest.setInstructedAmount(amount);
        paymentRequest.setRemittanceInformationUnstructured("Invoice 123");

        payment = new PSDPaymentDTO();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPaymentType("sepa-credit-transfers");
        payment.setTransactionStatus("ACCP");

        paymentStatus = new PSDPaymentStatusDTO();
        paymentStatus.setPaymentId(PAYMENT_ID);
        paymentStatus.setTransactionStatus("ACCP");
    }

    @Test
    void initiatePayment_shouldReturnPayment_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(true));
        when(paymentServicePort.initiatePayment(paymentRequest)).thenReturn(Mono.just(payment));

        // When & Then
        StepVerifier.create(paymentInitiationService.initiatePayment(CONSENT_ID, paymentRequest))
                .expectNext(payment)
                .verifyComplete();
    }

    @Test
    void initiatePayment_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(paymentInitiationService.initiatePayment(CONSENT_ID, paymentRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getPaymentStatus_shouldReturnStatus_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "READ")).thenReturn(Mono.just(true));
        when(paymentServicePort.getPaymentStatus(PAYMENT_ID)).thenReturn(Mono.just(paymentStatus));

        // When & Then
        StepVerifier.create(paymentInitiationService.getPaymentStatus(CONSENT_ID, PAYMENT_ID))
                .expectNext(paymentStatus)
                .verifyComplete();
    }

    @Test
    void getPaymentStatus_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(paymentInitiationService.getPaymentStatus(CONSENT_ID, PAYMENT_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getPayment_shouldReturnPayment_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "READ")).thenReturn(Mono.just(true));
        when(paymentServicePort.getPayment(PAYMENT_ID)).thenReturn(Mono.just(payment));

        // When & Then
        StepVerifier.create(paymentInitiationService.getPayment(CONSENT_ID, PAYMENT_ID))
                .expectNext(payment)
                .verifyComplete();
    }

    @Test
    void getPayment_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "READ")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(paymentInitiationService.getPayment(CONSENT_ID, PAYMENT_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void cancelPayment_shouldReturnCancelledPayment_whenConsentIsValid() {
        // Given
        PSDPaymentDTO cancelledPayment = new PSDPaymentDTO();
        cancelledPayment.setPaymentId(PAYMENT_ID);
        cancelledPayment.setTransactionStatus("CANC");

        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(true));
        when(paymentServicePort.cancelPayment(PAYMENT_ID)).thenReturn(Mono.just(true));
        when(paymentServicePort.getPayment(PAYMENT_ID)).thenReturn(Mono.just(cancelledPayment));

        // When & Then
        StepVerifier.create(paymentInitiationService.cancelPayment(CONSENT_ID, PAYMENT_ID))
                .expectNext(cancelledPayment)
                .verifyComplete();
    }

    @Test
    void cancelPayment_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(paymentInitiationService.cancelPayment(CONSENT_ID, PAYMENT_ID))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void authorizePayment_shouldReturnPayment_whenConsentIsValid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(true));
        when(paymentServicePort.authorizePayment(PAYMENT_ID, AUTHORIZATION_CODE)).thenReturn(Mono.just(payment));

        // When & Then
        StepVerifier.create(paymentInitiationService.authorizePayment(CONSENT_ID, PAYMENT_ID, AUTHORIZATION_CODE))
                .expectNext(payment)
                .verifyComplete();
    }

    @Test
    void authorizePayment_shouldReturnError_whenConsentIsInvalid() {
        // Given
        when(consentService.validateConsent(CONSENT_ID, "PAYMENT", "WRITE")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(paymentInitiationService.authorizePayment(CONSENT_ID, PAYMENT_ID, AUTHORIZATION_CODE))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
