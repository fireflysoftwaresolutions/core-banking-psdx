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


package io.fireflysoftware.core.banking.psdx.web.utils;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDLinksDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the LinkBuilder.
 */
class LinkBuilderTest {

    private static final String BASE_URL = "https://api.bank.com/v1";

    @Test
    void build_shouldCreateLinks() {
        // When
        PSDLinksDTO links = LinkBuilder.fromBaseUrl(BASE_URL)
                .withSelf("/accounts/12345")
                .withFirst("/accounts/12345/transactions?page=1")
                .withPrev("/accounts/12345/transactions?page=2")
                .withNext("/accounts/12345/transactions?page=4")
                .withLast("/accounts/12345/transactions?page=10")
                .withStatus("/accounts/12345/status")
                .withAccount("/accounts/12345")
                .withBalances("/accounts/12345/balances")
                .withTransactions("/accounts/12345/transactions")
                .withScaRedirect("/sca/redirect/12345")
                .withScaStatus("/sca/status/12345")
                .withConfirmation("/payments/12345/confirmation")
                .withAuthorization("/payments/12345/authorization")
                .withAdditionalLink("custom", "/custom/12345")
                .build();

        // Then
        assertEquals(BASE_URL + "/accounts/12345", links.getSelf().getHref());
        assertEquals(BASE_URL + "/accounts/12345/transactions?page=1", links.getFirst().getHref());
        assertEquals(BASE_URL + "/accounts/12345/transactions?page=2", links.getPrev().getHref());
        assertEquals(BASE_URL + "/accounts/12345/transactions?page=4", links.getNext().getHref());
        assertEquals(BASE_URL + "/accounts/12345/transactions?page=10", links.getLast().getHref());
        assertEquals(BASE_URL + "/accounts/12345/status", links.getStatus().getHref());
        assertEquals(BASE_URL + "/accounts/12345", links.getAccount().getHref());
        assertEquals(BASE_URL + "/accounts/12345/balances", links.getBalances().getHref());
        assertEquals(BASE_URL + "/accounts/12345/transactions", links.getTransactions().getHref());
        assertEquals(BASE_URL + "/sca/redirect/12345", links.getScaRedirect().getHref());
        assertEquals(BASE_URL + "/sca/status/12345", links.getScaStatus().getHref());
        assertEquals(BASE_URL + "/payments/12345/confirmation", links.getConfirmation().getHref());
        assertEquals(BASE_URL + "/payments/12345/authorization", links.getAuthorization().getHref());
        assertEquals(BASE_URL + "/custom/12345", links.getAdditionalLinks().get("custom").getHref());
    }

    @Test
    void build_shouldCreateLinksWithOnlySomeFields() {
        // When
        PSDLinksDTO links = LinkBuilder.fromBaseUrl(BASE_URL)
                .withSelf("/accounts/12345")
                .withAccount("/accounts/12345")
                .build();

        // Then
        assertEquals(BASE_URL + "/accounts/12345", links.getSelf().getHref());
        assertEquals(BASE_URL + "/accounts/12345", links.getAccount().getHref());
        assertNull(links.getFirst());
        assertNull(links.getPrev());
        assertNull(links.getNext());
        assertNull(links.getLast());
        assertNull(links.getStatus());
        assertNull(links.getBalances());
        assertNull(links.getTransactions());
        assertNull(links.getScaRedirect());
        assertNull(links.getScaStatus());
        assertNull(links.getConfirmation());
        assertNull(links.getAuthorization());
        assertNull(links.getAdditionalLinks());
    }
}
