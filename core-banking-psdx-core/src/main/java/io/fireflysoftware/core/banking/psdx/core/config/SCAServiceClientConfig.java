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

import io.fireflysoftware.common.sca.sdk.api.ScaChallengeControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaOperationControllerApi;
import io.fireflysoftware.common.sca.sdk.invoker.ApiClient;
import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxSCAServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkCalls;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkScaServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.SCAServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SCAServiceClientConfig {

    @Bean
    @ConditionalOnProperty(name = "integration.sca-mgmt.enabled", havingValue = "false", matchIfMissing = true)
    public SCAServicePort sandboxScaServicePort(
            @Value("${psdx.sca.required-for-all-payments:true}") boolean required,
            @Value("${psdx.sca.exemption-threshold-amount:30.00}") double amount,
            @Value("${psdx.sca.exemption-threshold-currency:EUR}") String currency) {
        return new SandboxSCAServiceAdapter(required, amount, currency);
    }

    @Bean
    @ConditionalOnProperty(name = "integration.sca-mgmt.enabled", havingValue = "true")
    public SCAServicePort coreScaServicePort(
            @Value("${integration.sca-mgmt.base-url}") String baseUrl,
            @Value("${psdx.sca.required-for-all-payments:true}") boolean required,
            @Value("${psdx.sca.exemption-threshold-amount:30.00}") double amount,
            @Value("${psdx.sca.exemption-threshold-currency:EUR}") String currency) {
        ApiClient client = new ApiClient();
        client.setBasePath(SdkCalls.base(baseUrl));
        return new SdkScaServiceAdapter(
                new ScaOperationControllerApi(client),
                new ScaChallengeControllerApi(client),
                required,
                amount,
                currency);
    }
}
