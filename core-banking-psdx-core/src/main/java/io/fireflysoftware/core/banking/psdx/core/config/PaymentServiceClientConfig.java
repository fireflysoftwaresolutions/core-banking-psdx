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

import io.fireflysoftware.core.banking.payments.sdk.api.PaymentOrdersApi;
import io.fireflysoftware.core.banking.payments.sdk.invoker.ApiClient;
import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxPaymentServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkCalls;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkPaymentServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.PaymentServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceClientConfig {

    @Bean
    @ConditionalOnProperty(name = "integration.payment-hub.enabled", havingValue = "false", matchIfMissing = true)
    public PaymentServicePort sandboxPaymentServicePort() {
        return new SandboxPaymentServiceAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "integration.payment-hub.enabled", havingValue = "true")
    public PaymentServicePort corePaymentServicePort(@Value("${integration.payment-hub.base-url}") String baseUrl) {
        ApiClient client = new ApiClient();
        client.setBasePath(SdkCalls.base(baseUrl));
        return new SdkPaymentServiceAdapter(new PaymentOrdersApi(client));
    }
}
