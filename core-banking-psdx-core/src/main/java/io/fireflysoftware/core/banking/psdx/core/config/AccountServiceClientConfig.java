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

package io.fireflysoftware.core.banking.psdx.core.config;

import io.fireflysoftware.core.banking.accounts.sdk.api.AccountBalancesApi;
import io.fireflysoftware.core.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.core.banking.accounts.sdk.invoker.ApiClient;
import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxAccountServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkAccountServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkCalls;
import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import io.fireflysoftware.core.contract.sdk.api.GlobalContractPartiesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sandbox accounts when {@code integration.accounts.enabled} is unset. The live bean calls the published SDKs.
 */
@Configuration
public class AccountServiceClientConfig {

    @Bean
    @ConditionalOnProperty(name = "integration.accounts.enabled", havingValue = "false", matchIfMissing = true)
    public AccountServicePort sandboxAccountServicePort() {
        return new SandboxAccountServiceAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "integration.accounts.enabled", havingValue = "true")
    public AccountServicePort coreAccountServicePort(
            @Value("${integration.accounts.base-url}") String accountsUrl,
            @Value("${integration.contracts.base-url}") String contractsUrl) {
        ApiClient accountsClient = new ApiClient();
        accountsClient.setBasePath(SdkCalls.base(accountsUrl));
        io.fireflysoftware.core.contract.sdk.invoker.ApiClient contractsClient =
                new io.fireflysoftware.core.contract.sdk.invoker.ApiClient();
        contractsClient.setBasePath(SdkCalls.base(contractsUrl));
        return new SdkAccountServiceAdapter(
                new AccountsApi(accountsClient),
                new AccountBalancesApi(accountsClient),
                new GlobalContractPartiesApi(contractsClient));
    }
}
