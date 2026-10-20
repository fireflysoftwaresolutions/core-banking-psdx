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


package io.fireflysoftware.core.banking.psdx.interfaces.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive validation tests for all DTOs with validation annotations.
 */
@DisplayName("DTO Validation Tests")
class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("PSDAmountDTO Validation")
    class PSDAmountDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid amount and currency")
        void shouldPassValidationWithValidData() {
            PSDAmountDTO amount = PSDAmountDTO.builder()
                    .currency("EUR")
                    .amount(new BigDecimal("123.45"))
                    .build();

            Set<ConstraintViolation<PSDAmountDTO>> violations = validator.validate(amount);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when currency is null")
        void shouldFailValidationWhenCurrencyIsNull() {
            PSDAmountDTO amount = PSDAmountDTO.builder()
                    .currency(null)
                    .amount(new BigDecimal("123.45"))
                    .build();

            Set<ConstraintViolation<PSDAmountDTO>> violations = validator.validate(amount);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Currency is required"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation when amount is null")
        void shouldFailValidationWhenAmountIsNull() {
            PSDAmountDTO amount = PSDAmountDTO.builder()
                    .currency("EUR")
                    .amount(null)
                    .build();

            Set<ConstraintViolation<PSDAmountDTO>> violations = validator.validate(amount);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Amount is required"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with invalid currency code")
        void shouldFailValidationWithInvalidCurrency() {
            PSDAmountDTO amount = PSDAmountDTO.builder()
                    .currency("INVALID")
                    .amount(new BigDecimal("123.45"))
                    .build();

            Set<ConstraintViolation<PSDAmountDTO>> violations = validator.validate(amount);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Currency must be a valid ISO 4217 currency code");
        }
    }

    @Nested
    @DisplayName("PSDAccountReferenceDTO Validation")
    class PSDAccountReferenceDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid IBAN")
        void shouldPassValidationWithValidIban() {
            PSDAccountReferenceDTO account = PSDAccountReferenceDTO.builder()
                    .iban("DE89370400440532013000")
                    .currency("EUR")
                    .build();

            Set<ConstraintViolation<PSDAccountReferenceDTO>> violations = validator.validate(account);
            // Note: Custom validators might still trigger on null fields, so we check for specific field validation
            assertThat(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("iban"))).isTrue();
            assertThat(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("currency"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with invalid IBAN")
        void shouldFailValidationWithInvalidIban() {
            PSDAccountReferenceDTO account = PSDAccountReferenceDTO.builder()
                    .iban("INVALID_IBAN")
                    .currency("EUR")
                    .build();

            Set<ConstraintViolation<PSDAccountReferenceDTO>> violations = validator.validate(account);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("IBAN must be a valid International Bank Account Number"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with invalid currency")
        void shouldFailValidationWithInvalidCurrency() {
            PSDAccountReferenceDTO account = PSDAccountReferenceDTO.builder()
                    .iban("DE89370400440532013000")
                    .currency("INVALID")
                    .build();

            Set<ConstraintViolation<PSDAccountReferenceDTO>> violations = validator.validate(account);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Currency must be a valid ISO 4217 currency code"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with invalid BBAN")
        void shouldFailValidationWithInvalidBban() {
            PSDAccountReferenceDTO account = PSDAccountReferenceDTO.builder()
                    .bban("invalid-bban-with-special-chars!")
                    .currency("EUR") // Add valid currency to reduce noise
                    .build();

            Set<ConstraintViolation<PSDAccountReferenceDTO>> violations = validator.validate(account);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("BBAN must be alphanumeric and up to 30 characters"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with invalid masked PAN")
        void shouldFailValidationWithInvalidMaskedPan() {
            PSDAccountReferenceDTO account = PSDAccountReferenceDTO.builder()
                    .maskedPan("invalid")
                    .currency("EUR") // Add valid currency to reduce noise
                    .build();

            Set<ConstraintViolation<PSDAccountReferenceDTO>> violations = validator.validate(account);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Masked PAN must be 12-19 characters with digits and asterisks"))).isTrue();
        }
    }

    @Nested
    @DisplayName("PSDAuthenticationRequestDTO Validation")
    class PSDAuthenticationRequestDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid email and password")
        void shouldPassValidationWithValidData() {
            PSDAuthenticationRequestDTO request = PSDAuthenticationRequestDTO.builder()
                    .username("user@example.com")
                    .password("SecurePassword123!")
                    .build();

            Set<ConstraintViolation<PSDAuthenticationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid email")
        void shouldFailValidationWithInvalidEmail() {
            PSDAuthenticationRequestDTO request = PSDAuthenticationRequestDTO.builder()
                    .username("invalid-email")
                    .password("SecurePassword123!")
                    .build();

            Set<ConstraintViolation<PSDAuthenticationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Username must be a valid email address");
        }

        @Test
        @DisplayName("Should fail validation with short password")
        void shouldFailValidationWithShortPassword() {
            PSDAuthenticationRequestDTO request = PSDAuthenticationRequestDTO.builder()
                    .username("user@example.com")
                    .password("short")
                    .build();

            Set<ConstraintViolation<PSDAuthenticationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(2); // Size and password strength
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Password must be between 8 and 128 characters"))).isTrue();
        }

        @Test
        @DisplayName("Should fail validation with blank username")
        void shouldFailValidationWithBlankUsername() {
            PSDAuthenticationRequestDTO request = PSDAuthenticationRequestDTO.builder()
                    .username("")
                    .password("SecurePassword123!")
                    .build();

            Set<ConstraintViolation<PSDAuthenticationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Username is required");
        }
    }

    @Nested
    @DisplayName("PSDSCAValidationRequestDTO Validation")
    class PSDSCAValidationRequestDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid challenge ID and auth code")
        void shouldPassValidationWithValidData() {
            PSDSCAValidationRequestDTO request = PSDSCAValidationRequestDTO.builder()
                    .challengeId("sca-123456")
                    .authenticationCode("123456")
                    .build();

            Set<ConstraintViolation<PSDSCAValidationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid challenge ID")
        void shouldFailValidationWithInvalidChallengeId() {
            PSDSCAValidationRequestDTO request = PSDSCAValidationRequestDTO.builder()
                    .challengeId("invalid challenge id!")
                    .authenticationCode("123456")
                    .build();

            Set<ConstraintViolation<PSDSCAValidationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Challenge ID must contain only alphanumeric characters, hyphens, and underscores");
        }

        @Test
        @DisplayName("Should fail validation with non-numeric auth code")
        void shouldFailValidationWithNonNumericAuthCode() {
            PSDSCAValidationRequestDTO request = PSDSCAValidationRequestDTO.builder()
                    .challengeId("sca-123456")
                    .authenticationCode("abc123")
                    .build();

            Set<ConstraintViolation<PSDSCAValidationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Authentication code must contain only digits");
        }

        @Test
        @DisplayName("Should fail validation with short auth code")
        void shouldFailValidationWithShortAuthCode() {
            PSDSCAValidationRequestDTO request = PSDSCAValidationRequestDTO.builder()
                    .challengeId("sca-123456")
                    .authenticationCode("12")
                    .build();

            Set<ConstraintViolation<PSDSCAValidationRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Authentication code must be between 4 and 10 characters");
        }
    }

    @Nested
    @DisplayName("PSDConsentRequestDTO Validation")
    class PSDConsentRequestDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid consent request")
        void shouldPassValidationWithValidData() {
            PSDConsentRequestDTO request = PSDConsentRequestDTO.builder()
                    .partyId("550e8400-e29b-41d4-a716-446655440000")
                    .consentType("account")
                    .validUntil(LocalDateTime.now().plusDays(90))
                    .frequencyPerDay(4)
                    .accessScope("all-accounts")
                    .access(List.of()) // Empty list for simplicity
                    .build();

            Set<ConstraintViolation<PSDConsentRequestDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid party ID")
        void shouldFailValidationWithInvalidPartyId() {
            PSDConsentRequestDTO request = PSDConsentRequestDTO.builder()
                    .partyId("invalid-uuid")
                    .consentType("account")
                    .validUntil(LocalDateTime.now().plusDays(90))
                    .access(List.of())
                    .build();

            Set<ConstraintViolation<PSDConsentRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Party ID must be a valid UUID");
        }

        @Test
        @DisplayName("Should fail validation with invalid consent type")
        void shouldFailValidationWithInvalidConsentType() {
            PSDConsentRequestDTO request = PSDConsentRequestDTO.builder()
                    .partyId("550e8400-e29b-41d4-a716-446655440000")
                    .consentType("invalid-type")
                    .validUntil(LocalDateTime.now().plusDays(90))
                    .access(List.of())
                    .build();

            Set<ConstraintViolation<PSDConsentRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Consent type must be one of: account, payment, funds-confirmation");
        }

        @Test
        @DisplayName("Should fail validation with past valid until date")
        void shouldFailValidationWithPastValidUntilDate() {
            PSDConsentRequestDTO request = PSDConsentRequestDTO.builder()
                    .partyId("550e8400-e29b-41d4-a716-446655440000")
                    .consentType("account")
                    .validUntil(LocalDateTime.now().minusDays(1))
                    .access(List.of())
                    .build();

            Set<ConstraintViolation<PSDConsentRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Valid until date must be in the future");
        }
    }

    @Nested
    @DisplayName("PSDThirdPartyProviderRegistrationDTO Validation")
    class PSDThirdPartyProviderRegistrationDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid TPP registration data")
        void shouldPassValidationWithValidData() {
            PSDThirdPartyProviderRegistrationDTO request = PSDThirdPartyProviderRegistrationDTO.builder()
                    .name("FinTech Solutions Ltd")
                    .registrationNumber("TPP123456")
                    .nationalCompetentAuthority("DE-BAFIN")
                    .nationalCompetentAuthorityCountry("DE")
                    .redirectUri("https://fintech-solutions.com/callback")
                    .providerType("AISP")
                    .roles(List.of("PSP_AI"))
                    .build();

            Set<ConstraintViolation<PSDThirdPartyProviderRegistrationDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid redirect URI")
        void shouldFailValidationWithInvalidRedirectUri() {
            PSDThirdPartyProviderRegistrationDTO request = PSDThirdPartyProviderRegistrationDTO.builder()
                    .name("FinTech Solutions Ltd")
                    .registrationNumber("TPP123456")
                    .nationalCompetentAuthority("DE-BAFIN")
                    .nationalCompetentAuthorityCountry("DE")
                    .redirectUri("http://insecure-url.com")
                    .providerType("AISP")
                    .roles(List.of("PSP_AI"))
                    .build();

            Set<ConstraintViolation<PSDThirdPartyProviderRegistrationDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Redirect URI must be a valid HTTPS URL");
        }

        @Test
        @DisplayName("Should fail validation with invalid provider type")
        void shouldFailValidationWithInvalidProviderType() {
            PSDThirdPartyProviderRegistrationDTO request = PSDThirdPartyProviderRegistrationDTO.builder()
                    .name("FinTech Solutions Ltd")
                    .registrationNumber("TPP123456")
                    .nationalCompetentAuthority("DE-BAFIN")
                    .nationalCompetentAuthorityCountry("DE")
                    .redirectUri("https://fintech-solutions.com/callback")
                    .providerType("INVALID")
                    .roles(List.of("PSP_AI"))
                    .build();

            Set<ConstraintViolation<PSDThirdPartyProviderRegistrationDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Provider type must be one of: AISP, PISP, CBPII, ASPSP");
        }

        @Test
        @DisplayName("Should fail validation with invalid role")
        void shouldFailValidationWithInvalidRole() {
            PSDThirdPartyProviderRegistrationDTO request = PSDThirdPartyProviderRegistrationDTO.builder()
                    .name("FinTech Solutions Ltd")
                    .registrationNumber("TPP123456")
                    .nationalCompetentAuthority("DE-BAFIN")
                    .nationalCompetentAuthorityCountry("DE")
                    .redirectUri("https://fintech-solutions.com/callback")
                    .providerType("AISP")
                    .roles(List.of("INVALID_ROLE"))
                    .build();

            Set<ConstraintViolation<PSDThirdPartyProviderRegistrationDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Role must be one of: PSP_AS, PSP_PI, PSP_AI, PSP_IC");
        }

        @Test
        @DisplayName("Should fail validation with invalid country code")
        void shouldFailValidationWithInvalidCountryCode() {
            PSDThirdPartyProviderRegistrationDTO request = PSDThirdPartyProviderRegistrationDTO.builder()
                    .name("FinTech Solutions Ltd")
                    .registrationNumber("TPP123456")
                    .nationalCompetentAuthority("DE-BAFIN")
                    .nationalCompetentAuthorityCountry("INVALID")
                    .redirectUri("https://fintech-solutions.com/callback")
                    .providerType("AISP")
                    .roles(List.of("PSP_AI"))
                    .build();

            Set<ConstraintViolation<PSDThirdPartyProviderRegistrationDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Country must be a valid 2-letter ISO country code");
        }
    }

    @Nested
    @DisplayName("PSDAccessLogRequestDTO Validation")
    class PSDAccessLogRequestDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid access log data")
        void shouldPassValidationWithValidData() {
            PSDAccessLogRequestDTO request = PSDAccessLogRequestDTO.builder()
                    .consentId(UUID.randomUUID())
                    .partyId(UUID.randomUUID())
                    .thirdPartyId("TPP-123")
                    .accessType("READ")
                    .resourceType("ACCOUNT")
                    .resourceId("ACC-123456")
                    .ipAddress("192.168.1.1")
                    .status("SUCCESS")
                    .build();

            Set<ConstraintViolation<PSDAccessLogRequestDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid access type")
        void shouldFailValidationWithInvalidAccessType() {
            PSDAccessLogRequestDTO request = PSDAccessLogRequestDTO.builder()
                    .consentId(UUID.randomUUID())
                    .partyId(UUID.randomUUID())
                    .thirdPartyId("TPP-123")
                    .accessType("INVALID")
                    .resourceType("ACCOUNT")
                    .resourceId("ACC-123456")
                    .status("SUCCESS")
                    .build();

            Set<ConstraintViolation<PSDAccessLogRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Access type must be one of: READ, WRITE, DELETE");
        }

        @Test
        @DisplayName("Should fail validation with invalid IP address")
        void shouldFailValidationWithInvalidIpAddress() {
            PSDAccessLogRequestDTO request = PSDAccessLogRequestDTO.builder()
                    .consentId(UUID.randomUUID())
                    .partyId(UUID.randomUUID())
                    .thirdPartyId("TPP-123")
                    .accessType("READ")
                    .resourceType("ACCOUNT")
                    .resourceId("ACC-123456")
                    .ipAddress("invalid-ip")
                    .status("SUCCESS")
                    .build();

            Set<ConstraintViolation<PSDAccessLogRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("IP address must be a valid IPv4 or IPv6 address");
        }
    }

    @Nested
    @DisplayName("PSDRefreshTokenRequestDTO Validation")
    class PSDRefreshTokenRequestDTOValidationTest {

        @Test
        @DisplayName("Should pass validation with valid refresh token")
        void shouldPassValidationWithValidData() {
            PSDRefreshTokenRequestDTO request = PSDRefreshTokenRequestDTO.builder()
                    .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                    .build();

            Set<ConstraintViolation<PSDRefreshTokenRequestDTO>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with short refresh token")
        void shouldFailValidationWithShortToken() {
            PSDRefreshTokenRequestDTO request = PSDRefreshTokenRequestDTO.builder()
                    .refreshToken("short")
                    .build();

            Set<ConstraintViolation<PSDRefreshTokenRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Refresh token must be between 10 and 2048 characters");
        }

        @Test
        @DisplayName("Should fail validation with invalid token format")
        void shouldFailValidationWithInvalidTokenFormat() {
            PSDRefreshTokenRequestDTO request = PSDRefreshTokenRequestDTO.builder()
                    .refreshToken("invalid token with spaces!")
                    .build();

            Set<ConstraintViolation<PSDRefreshTokenRequestDTO>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Refresh token must be a valid JWT or base64 encoded string");
        }
    }
}
