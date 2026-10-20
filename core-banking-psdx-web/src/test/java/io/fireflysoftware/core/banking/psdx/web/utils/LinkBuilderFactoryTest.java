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

/**
 * Tests for the LinkBuilderFactory.
 */
class LinkBuilderFactoryTest {

    private static final String BASE_URL = "https://api.bank.com/v1";

    @Test
    void create_shouldCreateLinkBuilder() {
        // Given
        LinkBuilderFactory factory = new LinkBuilderFactory(BASE_URL);

        // When
        LinkBuilder linkBuilder = factory.create();
        PSDLinksDTO links = linkBuilder.withSelf("/accounts/12345").build();

        // Then
        assertEquals(BASE_URL + "/accounts/12345", links.getSelf().getHref());
    }
}
