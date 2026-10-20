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

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogDTO;
import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDAccessLogRequestDTO;
import io.fireflysoftware.core.banking.psdx.models.entities.AccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between AccessLog entity and AccessLogDTO.
 */
@Mapper(componentModel = "spring")
public interface AccessLogMapper {

    /**
     * Convert an AccessLog entity to an AccessLogDTO.
     *
     * @param accessLog The AccessLog entity
     * @return The AccessLogDTO
     */
    @Mapping(target = "accessType", expression = "java(accessLog.getAccessType().name())")
    @Mapping(target = "resourceType", expression = "java(accessLog.getResourceType().name())")
    @Mapping(target = "status", expression = "java(accessLog.getStatus().name())")
    @Mapping(target = "xRequestId", source = "XRequestId")
    @Mapping(target = "tppRequestId", source = "tppRequestId")
    @Mapping(target = "psuId", source = "psuId")
    @Mapping(target = "psuIdType", source = "psuIdType")
    @Mapping(target = "psuCorporateId", source = "psuCorporateId")
    @Mapping(target = "psuCorporateIdType", source = "psuCorporateIdType")
    @Mapping(target = "tppRedirectUri", source = "tppRedirectUri")
    PSDAccessLogDTO toDto(AccessLog accessLog);

    /**
     * Convert an AccessLogDTO to an AccessLog entity.
     *
     * @param accessLogDTO The AccessLogDTO
     * @return The AccessLog entity
     */
    @Mapping(target = "accessType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType.valueOf(accessLogDTO.getAccessType()))")
    @Mapping(target = "resourceType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType.valueOf(accessLogDTO.getResourceType()))")
    @Mapping(target = "status", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus.valueOf(accessLogDTO.getStatus()))")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "xRequestId", source = "XRequestId")
    @Mapping(target = "tppRequestId", source = "tppRequestId")
    @Mapping(target = "psuId", source = "psuId")
    @Mapping(target = "psuIdType", source = "psuIdType")
    @Mapping(target = "psuCorporateId", source = "psuCorporateId")
    @Mapping(target = "psuCorporateIdType", source = "psuCorporateIdType")
    @Mapping(target = "tppRedirectUri", source = "tppRedirectUri")
    AccessLog toEntity(PSDAccessLogDTO accessLogDTO);

    /**
     * Convert an AccessLogRequestDTO to an AccessLog entity.
     *
     * @param request The AccessLogRequestDTO
     * @return The AccessLog entity
     */
    @Mapping(target = "accessType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType.valueOf(request.getAccessType()))")
    @Mapping(target = "resourceType", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType.valueOf(request.getResourceType()))")
    @Mapping(target = "status", expression = "java(io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus.valueOf(request.getStatus()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "xRequestId", source = "XRequestId")
    @Mapping(target = "tppRequestId", source = "tppRequestId")
    @Mapping(target = "psuId", source = "psuId")
    @Mapping(target = "psuIdType", source = "psuIdType")
    @Mapping(target = "psuCorporateId", source = "psuCorporateId")
    @Mapping(target = "psuCorporateIdType", source = "psuCorporateIdType")
    @Mapping(target = "tppRedirectUri", source = "tppRedirectUri")
    AccessLog fromRequest(PSDAccessLogRequestDTO request);
}
