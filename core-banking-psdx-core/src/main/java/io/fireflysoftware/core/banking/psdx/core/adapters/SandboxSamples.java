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

package io.fireflysoftware.core.banking.psdx.core.adapters;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stable identifiers for in-memory sandbox samples. Nothing here opens a socket.
 */
final class SandboxSamples {

    static final LocalDate SAMPLE_DATE = LocalDate.of(2026, 1, 15);
    static final LocalDateTime SAMPLE_TIME = SAMPLE_DATE.atTime(9, 0);
    static final String SAMPLE_IBAN = "DE89370400440532013000";
    static final String SAMPLE_CURRENCY = "EUR";

    private SandboxSamples() {
    }

    static UUID id(String kind, String material) {
        String value = kind + ":" + (material == null ? "absent" : material);
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    static String text(UUID value) {
        return value == null ? "absent" : value.toString();
    }

    /**
     * Code derived from a challenge id. The literal challenge code 123456 is never produced.
     */
    static String challengeCode(String challengeId) {
        String derived = id("sca-code", challengeId).toString().replace("-", "");
        String code = derived.substring(0, 8);
        if ("123456".equals(code) || "sandbox".equalsIgnoreCase(code)) {
            code = derived.substring(8, 16);
        }
        return code;
    }
}
