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


package io.fireflysoftware.core.banking.psdx.core.config;

import io.fireflysoftware.core.banking.psdx.core.adapters.DefaultCardServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.CardServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for the Card service client.
 * This configuration provides a bean for the Card service port.
 * In a real implementation, this would be replaced with a client for the core-banking-cards service.
 */
@Configuration
public class CardServiceClientConfig {

    @Value("${integration.cards.enabled:false}")
    private boolean cardsEnabled;

    /**
     * Create a bean for the Card service port.
     * If the Cards service is enabled, this would return a client for that service.
     * Otherwise, it returns the default adapter that throws "not yet implemented" exceptions.
     *
     * @param defaultAdapter The default adapter
     * @return The Card service port
     */
    @Bean
    @Primary
    public CardServicePort cardServicePort(DefaultCardServiceAdapter defaultAdapter) {
        // In a real implementation, this would return a client for the core-banking-cards service
        // if cardsEnabled is true. For now, we'll just return the default adapter.
        return defaultAdapter;
    }
}
