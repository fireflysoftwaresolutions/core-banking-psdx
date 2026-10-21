/*
 * Copyright 2026 Firefly Software Foundation
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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDPaymentInitiationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAAuthenticationRequestDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDSCAValidationRequestDTO;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SandboxAdapterTest {

    private static final UUID PARTY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Test
    void accountCardTransactionAndPaymentSamplesFinishWithoutThrowing() {
        var accounts = new SandboxAccountServiceAdapter();
        var cards = new SandboxCardServiceAdapter();
        var transactions = new SandboxTransactionServiceAdapter();
        var payments = new SandboxPaymentServiceAdapter();

        StepVerifier.create(accounts.getAccountsByPartyId(PARTY_ID))
                .assertNext(account -> assertThat(account.getOwnerPartyId()).isEqualTo(PARTY_ID))
                .verifyComplete();
        StepVerifier.create(accounts.getAccountsByPartyId(PARTY_ID).map(account -> account.getResourceId()))
                .expectNext(accounts.getAccountsByPartyId(PARTY_ID).blockFirst().getResourceId())
                .verifyComplete();

        StepVerifier.create(cards.getCardAccountsByPartyId(PARTY_ID))
                .assertNext(card -> assertThat(card.getStatus()).isEqualTo("enabled"))
                .verifyComplete();
        StepVerifier.create(transactions.getTransactionsByAccountId(PARTY_ID, null, null))
                .assertNext(transaction -> assertThat(transaction.getTransactionStatus()).isEqualTo("booked"))
                .verifyComplete();

        var request = new PSDPaymentInitiationRequestDTO();
        request.setPaymentType("sepa-credit-transfers");
        request.setCreditorName("Ada Lovelace");
        var amount = new PSDPaymentInitiationRequestDTO.PSDAmountDTO();
        amount.setCurrency("EUR");
        amount.setAmount(new BigDecimal("18.00"));
        request.setInstructedAmount(amount);

        var first = payments.initiatePayment(request).block();
        var second = payments.initiatePayment(request).block();
        assertThat(first.getPaymentId()).isEqualTo(second.getPaymentId());
        assertThat(first.getTransactionStatus()).isEqualTo("ACSC");
        StepVerifier.create(payments.authorizePayment(first.getPaymentId(), "123456"))
                .assertNext(payment -> assertThat(payment.getTransactionStatus()).isEqualTo("ACSC"))
                .verifyComplete();
    }

    @Test
    void scaRejectsTheLiteralChallengeCode() {
        var sca = new SandboxSCAServiceAdapter();
        var issued = sca.initiateSCA(request()).block();
        assertThat(SandboxSamples.challengeCode(issued.getChallengeId())).isNotEqualTo("123456");
        assertThat(SandboxSamples.challengeCode(issued.getChallengeId())).isNotEqualToIgnoringCase("sandbox");

        var rejected = new PSDSCAValidationRequestDTO();
        rejected.setChallengeId(issued.getChallengeId());
        rejected.setAuthenticationCode("sandbox");
        StepVerifier.create(sca.validateSCA(rejected))
                .assertNext(result -> assertThat(result.getSuccess()).isFalse())
                .verifyComplete();

        var accepted = new PSDSCAValidationRequestDTO();
        accepted.setChallengeId(issued.getChallengeId());
        accepted.setAuthenticationCode(SandboxSamples.challengeCode(issued.getChallengeId()));
        StepVerifier.create(sca.validateSCA(accepted))
                .assertNext(result -> {
                    assertThat(result.getSuccess()).isTrue();
                    assertThat(result.getAuthenticationToken()).startsWith("sandbox-");
                })
                .verifyComplete();
    }

    private static PSDSCAAuthenticationRequestDTO request() {
        var request = new PSDSCAAuthenticationRequestDTO();
        request.setPartyId(PARTY_ID);
        request.setResourceId("payment-sample");
        request.setResourceType("PAYMENT");
        return request;
    }
}
