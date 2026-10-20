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


package io.fireflysoftware.core.banking.psdx.models.config;

import io.fireflysoftware.core.banking.psdx.interfaces.security.EncryptionService;
import io.fireflysoftware.core.banking.psdx.models.converters.DecryptedStringConverter;
import io.fireflysoftware.core.banking.psdx.models.converters.EncryptedStringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for R2DBC converters.
 */
@Configuration
@RequiredArgsConstructor
public class R2dbcConverterConfig {

    private final EncryptionService encryptionService;

    /**
     * Create a bean for R2DBC custom conversions.
     *
     * @return The R2DBC custom conversions
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new EncryptedStringConverter(encryptionService));
        converters.add(new DecryptedStringConverter(encryptionService));
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}
