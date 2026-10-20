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

import io.fireflysoftware.core.banking.psdx.core.adapters.DefaultAccountServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for the Account service client.
 * This configuration provides a bean for the Account service port.
 * In a real implementation, this would be replaced with a client for the core-banking-accounts service.
 */
@Configuration
public class AccountServiceClientConfig {

    @Value("${integration.accounts.enabled:false}")
    private boolean accountsEnabled;

    /**
     * Create a bean for the Account service port.
     * If the Accounts service is enabled, this would return a client for that service.
     * Otherwise, it returns the default adapter that throws "not yet implemented" exceptions.
     *
     * @param defaultAdapter The default adapter
     * @return The Account service port
     */
    @Bean
    @Primary
    public AccountServicePort accountServicePort(DefaultAccountServiceAdapter defaultAdapter) {
        // In a real implementation, this would return a client for the core-banking-accounts service
        // if accountsEnabled is true. For now, we'll just return the default adapter.
        return defaultAdapter;
    }
}
