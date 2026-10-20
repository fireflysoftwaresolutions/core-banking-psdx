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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

/**
 * Service for validating TPP certificates.
 */
@Service
@Slf4j
public class CertificateValidationService {

    private final boolean certificateValidationEnabled;

    /**
     * Constructor for CertificateValidationService.
     *
     * @param certificateValidationEnabled Whether certificate validation is enabled
     */
    public CertificateValidationService(
            @Value("${psdx.tpp.validation.certificate-validation:true}") boolean certificateValidationEnabled) {
        this.certificateValidationEnabled = certificateValidationEnabled;
        log.info("Certificate validation enabled: {}", certificateValidationEnabled);
    }

    /**
     * Validate a certificate.
     *
     * @param certificateContent The certificate content in Base64 format
     * @return True if the certificate is valid, false otherwise
     */
    public boolean validateCertificate(String certificateContent) {
        if (!certificateValidationEnabled || certificateContent == null || certificateContent.isEmpty()) {
            return true;
        }

        try {
            X509Certificate certificate = parseCertificate(certificateContent);
            return validateCertificate(certificate);
        } catch (Exception e) {
            log.error("Error validating certificate", e);
            return false;
        }
    }

    /**
     * Parse a certificate from Base64 content.
     *
     * @param certificateContent The certificate content in Base64 format
     * @return The X509Certificate
     * @throws CertificateException If the certificate cannot be parsed
     */
    public X509Certificate parseCertificate(String certificateContent) throws CertificateException {
        if (certificateContent == null || certificateContent.isEmpty()) {
            throw new CertificateException("Certificate content is null or empty");
        }

        try {
            // Remove PEM headers and footers if present
            String cleanContent = certificateContent
                    .replaceAll("-----BEGIN CERTIFICATE-----", "")
                    .replaceAll("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            byte[] certificateBytes = Base64.getDecoder().decode(cleanContent);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (IllegalArgumentException e) {
            throw new CertificateException("Invalid Base64 encoding", e);
        }
    }

    /**
     * Validate a certificate.
     *
     * @param certificate The X509Certificate
     * @return True if the certificate is valid, false otherwise
     */
    public boolean validateCertificate(X509Certificate certificate) {
        try {
            // Check if the certificate is expired
            Date now = new Date();
            certificate.checkValidity(now);

            // Additional validation can be added here, such as:
            // - Check if the certificate is revoked
            // - Check if the certificate is issued by a trusted CA
            // - Check if the certificate has the required extensions for PSD2

            return true;
        } catch (Exception e) {
            log.error("Certificate validation failed", e);
            return false;
        }
    }

    /**
     * Extract certificate information.
     *
     * @param certificateContent The certificate content in Base64 format
     * @return A CertificateInfo object containing the certificate information
     */
    public CertificateInfo extractCertificateInfo(String certificateContent) {
        if (certificateContent == null || certificateContent.isEmpty()) {
            return null;
        }

        try {
            X509Certificate certificate = parseCertificate(certificateContent);

            return CertificateInfo.builder()
                    .serialNumber(certificate.getSerialNumber().toString(16))
                    .subject(certificate.getSubjectX500Principal().getName())
                    .issuer(certificate.getIssuerX500Principal().getName())
                    .validFrom(LocalDateTime.ofInstant(certificate.getNotBefore().toInstant(), ZoneId.systemDefault()))
                    .validUntil(LocalDateTime.ofInstant(certificate.getNotAfter().toInstant(), ZoneId.systemDefault()))
                    .build();
        } catch (Exception e) {
            log.error("Error extracting certificate information", e);
            return null;
        }
    }

    /**
     * Class representing certificate information.
     */
    public static class CertificateInfo {
        private String serialNumber;
        private String subject;
        private String issuer;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;

        private CertificateInfo(Builder builder) {
            this.serialNumber = builder.serialNumber;
            this.subject = builder.subject;
            this.issuer = builder.issuer;
            this.validFrom = builder.validFrom;
            this.validUntil = builder.validUntil;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getSubject() {
            return subject;
        }

        public String getIssuer() {
            return issuer;
        }

        public LocalDateTime getValidFrom() {
            return validFrom;
        }

        public LocalDateTime getValidUntil() {
            return validUntil;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String serialNumber;
            private String subject;
            private String issuer;
            private LocalDateTime validFrom;
            private LocalDateTime validUntil;

            public Builder serialNumber(String serialNumber) {
                this.serialNumber = serialNumber;
                return this;
            }

            public Builder subject(String subject) {
                this.subject = subject;
                return this;
            }

            public Builder issuer(String issuer) {
                this.issuer = issuer;
                return this;
            }

            public Builder validFrom(LocalDateTime validFrom) {
                this.validFrom = validFrom;
                return this;
            }

            public Builder validUntil(LocalDateTime validUntil) {
                this.validUntil = validUntil;
                return this;
            }

            public CertificateInfo build() {
                return new CertificateInfo(this);
            }
        }
    }
}
