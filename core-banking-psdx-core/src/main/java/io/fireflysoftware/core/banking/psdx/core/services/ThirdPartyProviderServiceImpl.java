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
import io.fireflysoftware.core.banking.psdx.interfaces.services.ThirdPartyProviderService;
import io.fireflysoftware.core.banking.psdx.models.entities.ThirdPartyProvider;
import io.fireflysoftware.core.banking.psdx.models.repositories.ThirdPartyProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the ThirdPartyProviderService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdPartyProviderServiceImpl implements ThirdPartyProviderService {

    private final ThirdPartyProviderRepository thirdPartyProviderRepository;
    private final ThirdPartyProviderMapper thirdPartyProviderMapper;
    private final CertificateValidationService certificateValidationService;

    @Override
    public Mono<PSDThirdPartyProviderDTO> registerProvider(PSDThirdPartyProviderRegistrationDTO registration) {
        log.debug("Registering new third party provider: {}", registration.getName());

        // Validate certificate if provided
        if (registration.getCertificate() != null && registration.getCertificate().getContent() != null) {
            boolean isValid = certificateValidationService.validateCertificate(registration.getCertificate().getContent());
            if (!isValid) {
                return Mono.error(new IllegalArgumentException("Invalid certificate"));
            }

            // Extract certificate information
            CertificateValidationService.CertificateInfo certInfo =
                    certificateValidationService.extractCertificateInfo(registration.getCertificate().getContent());
            if (certInfo != null) {
                // Update certificate fields in the registration DTO
                registration.getCertificate().setSerialNumber(certInfo.getSerialNumber());
                registration.getCertificate().setSubject(certInfo.getSubject());
                registration.getCertificate().setIssuer(certInfo.getIssuer());
                registration.getCertificate().setValidFrom(certInfo.getValidFrom());
                registration.getCertificate().setValidUntil(certInfo.getValidUntil());
            }
        }

        // Generate a unique API key
        String apiKey = UUID.randomUUID().toString();

        // Convert registration DTO to entity using mapper
        ThirdPartyProvider provider = thirdPartyProviderMapper.toEntity(registration);
        provider.setApiKey(apiKey);
        provider.setCreatedAt(LocalDateTime.now());
        provider.setUpdatedAt(LocalDateTime.now());

        return thirdPartyProviderRepository.save(provider)
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> log.info("Third party provider registered with ID: {}", dto.getId()));
    }

    @Override
    public Mono<PSDThirdPartyProviderDTO> getProvider(UUID providerId) {
        log.debug("Getting third party provider with ID: {}", providerId);

        return thirdPartyProviderRepository.findById(providerId)
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> {
                    if (dto != null) {
                        log.debug("Found third party provider with ID: {}", providerId);
                    } else {
                        log.debug("Third party provider not found with ID: {}", providerId);
                    }
                });
    }

    @Override
    public Flux<PSDThirdPartyProviderDTO> getAllProviders() {
        log.debug("Getting all third party providers");

        return thirdPartyProviderRepository.findAll()
                .map(thirdPartyProviderMapper::toDto)
                .doOnComplete(() -> log.debug("Retrieved all third party providers"));
    }

    @Override
    public Mono<PSDThirdPartyProviderDTO> updateProvider(UUID providerId, PSDThirdPartyProviderDTO providerUpdate) {
        log.debug("Updating third party provider with ID: {}", providerId);

        return thirdPartyProviderRepository.findById(providerId)
                .flatMap(provider -> {
                    // Convert DTO to entity using mapper, but preserve ID and API key
                    ThirdPartyProvider updatedProvider = thirdPartyProviderMapper.toEntity(providerUpdate);
                    updatedProvider.setId(provider.getId());
                    updatedProvider.setApiKey(provider.getApiKey());
                    updatedProvider.setCreatedAt(provider.getCreatedAt());
                    updatedProvider.setUpdatedAt(LocalDateTime.now());
                    return thirdPartyProviderRepository.save(updatedProvider);
                })
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> log.info("Updated third party provider with ID: {}", providerId));
    }

    @Override
    public Mono<PSDThirdPartyProviderDTO> suspendProvider(UUID providerId) {
        log.debug("Suspending third party provider with ID: {}", providerId);

        return thirdPartyProviderRepository.findById(providerId)
                .flatMap(provider -> {
                    provider.setStatus(ProviderStatus.SUSPENDED);
                    provider.setUpdatedAt(LocalDateTime.now());
                    return thirdPartyProviderRepository.save(provider);
                })
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> log.info("Suspended third party provider with ID: {}", providerId));
    }

    @Override
    public Mono<PSDThirdPartyProviderDTO> activateProvider(UUID providerId) {
        log.debug("Activating third party provider with ID: {}", providerId);

        return thirdPartyProviderRepository.findById(providerId)
                .flatMap(provider -> {
                    provider.setStatus(ProviderStatus.ACTIVE);
                    provider.setUpdatedAt(LocalDateTime.now());
                    return thirdPartyProviderRepository.save(provider);
                })
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> log.info("Activated third party provider with ID: {}", providerId));
    }

    @Override
    public Mono<Boolean> revokeProvider(UUID providerId) {
        log.debug("Revoking third party provider with ID: {}", providerId);

        return thirdPartyProviderRepository.findById(providerId)
                .flatMap(provider -> {
                    provider.setStatus(ProviderStatus.REVOKED);
                    provider.setUpdatedAt(LocalDateTime.now());
                    return thirdPartyProviderRepository.save(provider)
                            .thenReturn(true);
                })
                .defaultIfEmpty(false)
                .doOnSuccess(result -> {
                    if (result) {
                        log.info("Revoked third party provider with ID: {}", providerId);
                    } else {
                        log.warn("Failed to revoke third party provider with ID: {}", providerId);
                    }
                });
    }

    @Override
    public Mono<PSDThirdPartyProviderDTO> validateApiKey(String apiKey) {
        log.debug("Validating API key");

        return thirdPartyProviderRepository.findByApiKey(apiKey)
                .filter(provider -> provider.getStatus() == ProviderStatus.ACTIVE)
                .map(thirdPartyProviderMapper::toDto)
                .doOnSuccess(dto -> {
                    if (dto != null) {
                        log.debug("API key validated successfully");
                    } else {
                        log.debug("API key validation failed");
                    }
                });
    }
}
