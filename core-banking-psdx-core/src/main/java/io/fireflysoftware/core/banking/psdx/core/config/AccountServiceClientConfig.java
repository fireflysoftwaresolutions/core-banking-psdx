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

import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxAccountServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.AccountServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Account rows live in core-banking-accounts. This core keeps the sandbox sample and does not call that core.
 * domain-banking-psdx reads accounts and contract parties.
 */
@Configuration
public class AccountServiceClientConfig {

    @Bean
    public AccountServicePort accountServicePort() {
        return new SandboxAccountServiceAdapter();
    }
}
