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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CertificateValidationService.
 */
class CertificateValidationServiceTest {

    private CertificateValidationService certificateValidationService;

    @BeforeEach
    void setUp() {
        certificateValidationService = new CertificateValidationService(true);
    }

    @Test
    void validateCertificate_withNullCertificate_shouldReturnTrue() {
        // When
        boolean result = certificateValidationService.validateCertificate((String) null);

        // Then
        assertTrue(result);
    }

    @Test
    void validateCertificate_withEmptyCertificate_shouldReturnTrue() {
        // When
        boolean result = certificateValidationService.validateCertificate("");

        // Then
        assertTrue(result);
    }

    @Test
    void validateCertificate_withInvalidCertificate_shouldReturnFalse() {
        // Given
        String invalidCertificate = "invalid certificate content";

        // When
        boolean result = certificateValidationService.validateCertificate(invalidCertificate);

        // Then
        assertFalse(result);
    }

    @Test
    void extractCertificateInfo_withNullCertificate_shouldReturnNull() {
        // When
        CertificateValidationService.CertificateInfo result = certificateValidationService.extractCertificateInfo(null);

        // Then
        assertNull(result);
    }

    @Test
    void extractCertificateInfo_withEmptyCertificate_shouldReturnNull() {
        // When
        CertificateValidationService.CertificateInfo result = certificateValidationService.extractCertificateInfo("");

        // Then
        assertNull(result);
    }

    @Test
    void extractCertificateInfo_withInvalidCertificate_shouldReturnNull() {
        // Given
        String invalidCertificate = "invalid certificate content";

        // When
        CertificateValidationService.CertificateInfo result = certificateValidationService.extractCertificateInfo(invalidCertificate);

        // Then
        assertNull(result);
    }

    // Additional tests would be added for valid certificates, but that would require
    // generating test certificates, which is beyond the scope of this example.
}
