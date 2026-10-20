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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDConsentDTO;
import io.fireflysoftware.core.banking.psdx.models.entities.Consent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Consent entity and ConsentDTO.
 */
@Mapper(componentModel = "spring")
public interface ConsentMapper {

    /**
     * Convert a Consent entity to a ConsentDTO.
     *
     * @param consent The Consent entity
     * @return The ConsentDTO
     */
    @Mapping(target = "consentType", expression = "java(consent.getConsentType().name())")
    @Mapping(target = "consentStatus", expression = "java(consent.getStatus().name())")
    @Mapping(target = "accessFrequency", source = "accessFrequency")
    @Mapping(target = "frequencyPerDay", source = "accessFrequency")
    @Mapping(target = "accessScope", source = "accessScope")
    @Mapping(target = "lastActionDate", ignore = true)
    @Mapping(target = "access", ignore = true)
    @Mapping(target = "combinedServiceIndicator", ignore = true)
    @Mapping(target = "recurringIndicator", ignore = true)
    @Mapping(target = "_links", ignore = true)
    PSDConsentDTO toDto(Consent consent);

    /**
     * Convert a ConsentDTO to a Consent entity.
     *
     * @param consentDTO The ConsentDTO
     * @return The Consent entity
     */
    @Mapping(target = "consentType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType.valueOf(consentDTO.getConsentType()))")
    @Mapping(target = "status", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus.valueOf(consentDTO.getConsentStatus()))")
    @Mapping(target = "accessFrequency", source = "accessFrequency")
    @Mapping(target = "accessScope", source = "accessScope")
    @Mapping(target = "access", ignore = true)
    Consent toEntity(PSDConsentDTO consentDTO);

    /**
     * Convert a list of PSDAccessDTO to a string.
     *
     * @param accessList The list of PSDAccessDTO
     * @return The string representation
     */
    default String map(java.util.List<PSDConsentDTO.PSDAccessDTO> accessList) {
        if (accessList == null || accessList.isEmpty()) {
            return null;
        }
        return accessList.stream()
                .map(access -> access.getResourceId())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(","));
    }
}