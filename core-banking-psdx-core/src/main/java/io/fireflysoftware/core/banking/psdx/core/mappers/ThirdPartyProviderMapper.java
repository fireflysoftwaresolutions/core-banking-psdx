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


package io.fireflysoftware.core.banking.psdx.core.mappers;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDThirdPartyProviderRegistrationDTO;
import io.fireflysoftware.core.banking.psdx.models.entities.ThirdPartyProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between ThirdPartyProvider entity and ThirdPartyProviderDTO.
 */
@Mapper(componentModel = "spring")
public interface ThirdPartyProviderMapper {

    /**
     * Convert a ThirdPartyProvider entity to a ThirdPartyProviderDTO.
     *
     * @param provider The ThirdPartyProvider entity
     * @return The ThirdPartyProviderDTO
     */
    @Mapping(target = "status", expression = "java(provider.getStatus().name())")
    @Mapping(target = "providerType", expression = "java(provider.getProviderType().name())")
    @Mapping(target = "nationalCompetentAuthority", source = "nationalCompetentAuthority")
    @Mapping(target = "nationalCompetentAuthorityCountry", source = "nationalCompetentAuthorityCountry")
    @Mapping(target = "roles", expression = "java(splitRoles(provider.getRoles()))")
    @Mapping(target = "certificate", expression = "java(mapCertificate(provider))")
    @Mapping(target = "_links", ignore = true)
    PSDThirdPartyProviderDTO toDto(ThirdPartyProvider provider);

    /**
     * Convert a ThirdPartyProviderRegistrationDTO to a ThirdPartyProvider entity.
     *
     * @param registrationDTO The ThirdPartyProviderRegistrationDTO
     * @return The ThirdPartyProvider entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "providerType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderType.valueOf(registrationDTO.getProviderType()))")
    @Mapping(target = "roles", expression = "java(joinRoles(registrationDTO.getRoles()))")
    @Mapping(target = "certificateSerialNumber", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getSerialNumber() : null)")
    @Mapping(target = "certificateSubject", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getSubject() : null)")
    @Mapping(target = "certificateIssuer", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getIssuer() : null)")
    @Mapping(target = "certificateValidFrom", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getValidFrom() : null)")
    @Mapping(target = "certificateValidUntil", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getValidUntil() : null)")
    @Mapping(target = "certificateContent", expression = "java(registrationDTO.getCertificate() != null ? registrationDTO.getCertificate().getContent() : null)")
    @Mapping(target = "apiKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ThirdPartyProvider toEntity(PSDThirdPartyProviderRegistrationDTO registrationDTO);

    /**
     * Convert a ThirdPartyProviderDTO to a ThirdPartyProvider entity.
     *
     * @param providerDTO The ThirdPartyProviderDTO
     * @return The ThirdPartyProvider entity
     */
    @Mapping(target = "status", expression = "java(ProviderStatus.valueOf(providerDTO.getStatus()))")
    @Mapping(target = "providerType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderType.valueOf(providerDTO.getProviderType()))")
    @Mapping(target = "roles", expression = "java(joinRoles(providerDTO.getRoles()))")
    @Mapping(target = "certificateSerialNumber", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getSerialNumber() : null)")
    @Mapping(target = "certificateSubject", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getSubject() : null)")
    @Mapping(target = "certificateIssuer", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getIssuer() : null)")
    @Mapping(target = "certificateValidFrom", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getValidFrom() : null)")
    @Mapping(target = "certificateValidUntil", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getValidUntil() : null)")
    @Mapping(target = "certificateContent", expression = "java(providerDTO.getCertificate() != null ? providerDTO.getCertificate().getContent() : null)")
    @Mapping(target = "apiKey", ignore = true)
    ThirdPartyProvider toEntity(PSDThirdPartyProviderDTO providerDTO);

    /**
     * Split a comma-separated string of roles into a list.
     *
     * @param roles The comma-separated string of roles
     * @return The list of roles
     */
    @Named("splitRoles")
    default List<String> splitRoles(String roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Join a list of roles into a comma-separated string.
     *
     * @param roles The list of roles
     * @return The comma-separated string of roles
     */
    @Named("joinRoles")
    default String joinRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return String.join(",", roles);
    }

    /**
     * Map certificate fields from a ThirdPartyProvider entity to a PSDCertificateDTO.
     *
     * @param provider The ThirdPartyProvider entity
     * @return The PSDCertificateDTO
     */
    @Named("mapCertificate")
    default PSDThirdPartyProviderDTO.PSDCertificateDTO mapCertificate(ThirdPartyProvider provider) {
        if (provider.getCertificateSerialNumber() == null &&
            provider.getCertificateSubject() == null &&
            provider.getCertificateIssuer() == null) {
            return null;
        }

        return PSDThirdPartyProviderDTO.PSDCertificateDTO.builder()
                .serialNumber(provider.getCertificateSerialNumber())
                .subject(provider.getCertificateSubject())
                .issuer(provider.getCertificateIssuer())
                .validFrom(provider.getCertificateValidFrom())
                .validUntil(provider.getCertificateValidUntil())
                .content(provider.getCertificateContent())
                .build();
    }
}