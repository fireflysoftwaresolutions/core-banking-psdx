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

import io.fireflysoftware.core.banking.psdx.core.adapters.SandboxTransactionServiceAdapter;
import io.fireflysoftware.core.banking.psdx.core.ports.TransactionServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ledger transactions live in core-banking-ledger. This core keeps the sandbox sample.
 * domain-banking-psdx reads the ledger.
 */
@Configuration
public class TransactionServiceClientConfig {

    @Bean
    public TransactionServicePort transactionServicePort() {
        return new SandboxTransactionServiceAdapter();
    }
}
