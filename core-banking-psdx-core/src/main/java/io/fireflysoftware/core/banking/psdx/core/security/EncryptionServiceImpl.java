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


package io.fireflysoftware.core.banking.psdx.core.security;

import io.fireflysoftware.core.banking.psdx.interfaces.security.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of the EncryptionService.
 */
@Service
public class EncryptionServiceImpl extends EncryptionService {

    /**
     * Constructor for EncryptionServiceImpl.
     *
     * @param encryptionEnabled Whether encryption is enabled
     * @param algorithm The encryption algorithm
     * @param secretKey The secret key
     */
    public EncryptionServiceImpl(
            @Value("${psdx.security.encryption.enabled:false}") boolean encryptionEnabled,
            @Value("${psdx.security.encryption.algorithm:AES/GCM/NoPadding}") String algorithm,
            @Value("${psdx.security.encryption.secret-key:}") String secretKey) {
        super(encryptionEnabled, algorithm, secretKey);
    }
}
