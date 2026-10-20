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


package io.fireflysoftware.core.banking.psdx.web.integration;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderRegistrationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.services.ThirdPartyProviderService;
import io.fireflysoftware.core.banking.psdx.web.controllers.ThirdPartyProviderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the ThirdPartyProvider API.
 */
@ExtendWith(MockitoExtension.class)
public class ThirdPartyProviderIntegrationTest {

    private WebTestClient webTestClient;

    @Mock
    private ThirdPartyProviderService thirdPartyProviderService;

    @InjectMocks
    private ThirdPartyProviderController thirdPartyProviderController;

    // Test constants
    private static final UUID PROVIDER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");

    private PSDThirdPartyProviderDTO providerDTO;
    private PSDThirdPartyProviderRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        // Setup WebTestClient
        webTestClient = WebTestClient.bindToController(thirdPartyProviderController).build();

        // Setup provider DTO
        providerDTO = new PSDThirdPartyProviderDTO();
        providerDTO.setId(PROVIDER_ID);
        providerDTO.setName("Test TPP");
        providerDTO.setRegistrationNumber("TPP123456");
        providerDTO.setRedirectUri("https://test-tpp.com/callback");
        providerDTO.setStatus("ACTIVE");
        providerDTO.setProviderType("AISP");
        providerDTO.setRoles(Arrays.asList("AISP"));
        providerDTO.setNationalCompetentAuthority("DE-BAFIN");
        providerDTO.setNationalCompetentAuthorityCountry("DE");

        // Setup registration DTO
        registrationDTO = new PSDThirdPartyProviderRegistrationDTO();
        registrationDTO.setName("Test TPP");
        registrationDTO.setRegistrationNumber("TPP123456");
        registrationDTO.setRedirectUri("https://test-tpp.com/callback");
        registrationDTO.setProviderType("AISP");
        registrationDTO.setRoles(Arrays.asList("PSP_AI"));
        registrationDTO.setNationalCompetentAuthority("DE-BAFIN");
        registrationDTO.setNationalCompetentAuthorityCountry("DE");
    }

    @Test
    void registerProvider_shouldReturnRegisteredProvider() {
        // Given
        when(thirdPartyProviderService.registerProvider(any(PSDThirdPartyProviderRegistrationDTO.class)))
                .thenReturn(Mono.just(providerDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/providers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registrationDTO)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(providerDTO);
    }

    @Test
    void getProvider_shouldReturnProvider() {
        // Given
        when(thirdPartyProviderService.getProvider(PROVIDER_ID)).thenReturn(Mono.just(providerDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/providers/" + PROVIDER_ID)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(providerDTO);
    }

    @Test
    void getAllProviders_shouldReturnProviders() {
        // Given
        when(thirdPartyProviderService.getAllProviders()).thenReturn(Flux.just(providerDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/providers")
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PSDThirdPartyProviderDTO.class)
                .hasSize(1)
                .contains(providerDTO);
    }

    @Test
    void updateProvider_shouldReturnUpdatedProvider() {
        // Given
        when(thirdPartyProviderService.updateProvider(eq(PROVIDER_ID), any(PSDThirdPartyProviderDTO.class)))
                .thenReturn(Mono.just(providerDTO));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/providers/" + PROVIDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(providerDTO)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(providerDTO);
    }

    @Test
    void suspendProvider_shouldReturnSuspendedProvider() {
        // Given
        PSDThirdPartyProviderDTO suspendedProvider = new PSDThirdPartyProviderDTO();
        suspendedProvider.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"));
        suspendedProvider.setStatus("SUSPENDED");

        when(thirdPartyProviderService.suspendProvider(PROVIDER_ID)).thenReturn(Mono.just(suspendedProvider));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/providers/" + PROVIDER_ID + "/suspend")
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(suspendedProvider);
    }

    @Test
    void activateProvider_shouldReturnActivatedProvider() {
        // Given
        when(thirdPartyProviderService.activateProvider(PROVIDER_ID)).thenReturn(Mono.just(providerDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/providers/" + PROVIDER_ID + "/activate")
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(providerDTO);
    }

    @Test
    void revokeProvider_shouldReturnOk() {
        // Given
        when(thirdPartyProviderService.revokeProvider(PROVIDER_ID)).thenReturn(Mono.just(true));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/providers/" + PROVIDER_ID)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void validateApiKey_shouldReturnProvider() {
        // Given
        when(thirdPartyProviderService.validateApiKey("test-api-key")).thenReturn(Mono.just(providerDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/providers/validate")
                .header("X-API-KEY", "test-api-key")
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PSDThirdPartyProviderDTO.class)
                .isEqualTo(providerDTO);
    }
}
