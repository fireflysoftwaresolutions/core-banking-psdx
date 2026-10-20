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


package io.fireflysoftware.core.banking.psdx.core.services;

import io.fireflysoftware.core.banking.psdx.core.mappers.ThirdPartyProviderMapper;
import io.fireflysoftware.core.banking.psdx.core.security.CertificateValidationService;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderRegistrationDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderType;
import io.fireflysoftware.core.banking.psdx.models.entities.ThirdPartyProvider;
import io.fireflysoftware.core.banking.psdx.models.repositories.ThirdPartyProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the ThirdPartyProviderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ThirdPartyProviderServiceImplTest {

    @Mock
    private ThirdPartyProviderRepository thirdPartyProviderRepository;

    @Mock
    private ThirdPartyProviderMapper thirdPartyProviderMapper;

    @Mock
    private CertificateValidationService certificateValidationService;

    private ThirdPartyProviderServiceImpl thirdPartyProviderService;

    // Test constants
    private static final UUID PROVIDER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");

    private ThirdPartyProvider provider;
    private PSDThirdPartyProviderDTO providerDTO;
    private PSDThirdPartyProviderRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        thirdPartyProviderService = new ThirdPartyProviderServiceImpl(
                thirdPartyProviderRepository, thirdPartyProviderMapper, certificateValidationService);

        provider = new ThirdPartyProvider();
        provider.setId(PROVIDER_ID);
        provider.setName("Test Provider");
        provider.setRegistrationNumber("TPP123456");
        provider.setApiKey("test-api-key");
        provider.setRedirectUri("https://test.com/callback");
        provider.setStatus(ProviderStatus.ACTIVE);
        provider.setProviderType(ProviderType.AISP);
        provider.setCreatedAt(LocalDateTime.now());
        provider.setUpdatedAt(LocalDateTime.now());

        providerDTO = new PSDThirdPartyProviderDTO();
        providerDTO.setId(PROVIDER_ID);
        providerDTO.setName("Test Provider");
        providerDTO.setRegistrationNumber("TPP123456");
        providerDTO.setRedirectUri("https://test.com/callback");
        providerDTO.setStatus("ACTIVE");
        providerDTO.setProviderType("AISP");
        providerDTO.setRoles(List.of("AISP"));

        registrationDTO = new PSDThirdPartyProviderRegistrationDTO();
        registrationDTO.setName("Test Provider");
        registrationDTO.setRegistrationNumber("TPP123456");
        registrationDTO.setRedirectUri("https://test.com/callback");
        registrationDTO.setProviderType("AISP");
        registrationDTO.setRoles(List.of("AISP"));
        registrationDTO.setNationalCompetentAuthority("DE-BAFIN");
        registrationDTO.setNationalCompetentAuthorityCountry("DE");
    }

    @Test
    void registerProvider_shouldRegisterProvider() {
        // Given
        when(thirdPartyProviderMapper.toEntity(any(PSDThirdPartyProviderRegistrationDTO.class))).thenReturn(provider);
        when(thirdPartyProviderRepository.save(any(ThirdPartyProvider.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(any(ThirdPartyProvider.class))).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.registerProvider(registrationDTO);

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        verify(thirdPartyProviderMapper).toEntity(registrationDTO);
        verify(thirdPartyProviderRepository).save(any(ThirdPartyProvider.class));
        verify(thirdPartyProviderMapper).toDto(provider);
    }

    @Test
    void getProvider_shouldReturnProvider() {
        // Given
        when(thirdPartyProviderRepository.findById(any(UUID.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.getProvider(PROVIDER_ID);

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        verify(thirdPartyProviderRepository).findById(PROVIDER_ID);
        verify(thirdPartyProviderMapper).toDto(provider);
    }

    @Test
    void getAllProviders_shouldReturnAllProviders() {
        // Given
        when(thirdPartyProviderRepository.findAll()).thenReturn(Flux.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Flux<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.getAllProviders();

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        verify(thirdPartyProviderRepository).findAll();
        verify(thirdPartyProviderMapper).toDto(provider);
    }

    @Test
    void updateProvider_shouldUpdateProvider() {
        // Given
        when(thirdPartyProviderRepository.findById(any(UUID.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toEntity(providerDTO)).thenReturn(provider);
        when(thirdPartyProviderRepository.save(any(ThirdPartyProvider.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.updateProvider(PROVIDER_ID, providerDTO);

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        verify(thirdPartyProviderRepository).findById(PROVIDER_ID);
        verify(thirdPartyProviderMapper).toEntity(providerDTO);
        verify(thirdPartyProviderRepository).save(any(ThirdPartyProvider.class));
        verify(thirdPartyProviderMapper).toDto(provider);
    }

    @Test
    void suspendProvider_shouldSuspendProvider() {
        // Given
        when(thirdPartyProviderRepository.findById(any(UUID.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderRepository.save(any(ThirdPartyProvider.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.suspendProvider(UUID.randomUUID());

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        ArgumentCaptor<ThirdPartyProvider> providerCaptor = ArgumentCaptor.forClass(ThirdPartyProvider.class);
        verify(thirdPartyProviderRepository).save(providerCaptor.capture());
        assertEquals(ProviderStatus.SUSPENDED, providerCaptor.getValue().getStatus());
    }

    @Test
    void activateProvider_shouldActivateProvider() {
        // Given
        provider.setStatus(ProviderStatus.SUSPENDED);
        when(thirdPartyProviderRepository.findById(any(UUID.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderRepository.save(any(ThirdPartyProvider.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.activateProvider(UUID.randomUUID());

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        ArgumentCaptor<ThirdPartyProvider> providerCaptor = ArgumentCaptor.forClass(ThirdPartyProvider.class);
        verify(thirdPartyProviderRepository).save(providerCaptor.capture());
        assertEquals(ProviderStatus.ACTIVE, providerCaptor.getValue().getStatus());
    }

    @Test
    void revokeProvider_shouldRevokeProvider() {
        // Given
        when(thirdPartyProviderRepository.findById(any(UUID.class))).thenReturn(Mono.just(provider));
        when(thirdPartyProviderRepository.save(any(ThirdPartyProvider.class))).thenReturn(Mono.just(provider));

        // When
        Mono<Boolean> result = thirdPartyProviderService.revokeProvider(UUID.randomUUID());

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<ThirdPartyProvider> providerCaptor = ArgumentCaptor.forClass(ThirdPartyProvider.class);
        verify(thirdPartyProviderRepository).save(providerCaptor.capture());
        assertEquals(ProviderStatus.REVOKED, providerCaptor.getValue().getStatus());
    }

    @Test
    void validateApiKey_shouldReturnProvider() {
        // Given
        when(thirdPartyProviderRepository.findByApiKey("test-api-key")).thenReturn(Mono.just(provider));
        when(thirdPartyProviderMapper.toDto(provider)).thenReturn(providerDTO);

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.validateApiKey("test-api-key");

        // Then
        StepVerifier.create(result)
                .expectNext(providerDTO)
                .verifyComplete();

        verify(thirdPartyProviderRepository).findByApiKey("test-api-key");
        verify(thirdPartyProviderMapper).toDto(provider);
    }

    @Test
    void validateApiKey_withInvalidApiKey_shouldReturnEmpty() {
        // Given
        when(thirdPartyProviderRepository.findByApiKey("invalid-api-key")).thenReturn(Mono.empty());

        // When
        Mono<PSDThirdPartyProviderDTO> result = thirdPartyProviderService.validateApiKey("invalid-api-key");

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(thirdPartyProviderRepository).findByApiKey("invalid-api-key");
        verify(thirdPartyProviderMapper, never()).toDto(any());
    }
}
