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


package io.fireflysoftware.core.banking.psdx.models.converters;

import io.fireflysoftware.core.banking.psdx.interfaces.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for decrypting strings when reading from the database.
 */
@ReadingConverter
@RequiredArgsConstructor
public class DecryptedStringConverter implements Converter<String, String> {

    private final EncryptionService encryptionService;

    /**
     * Convert an encrypted string to a decrypted string.
     *
     * @param source The encrypted string
     * @return The decrypted string
     */
    @Override
    public String convert(String source) {
        return encryptionService.decrypt(source);
    }
}
