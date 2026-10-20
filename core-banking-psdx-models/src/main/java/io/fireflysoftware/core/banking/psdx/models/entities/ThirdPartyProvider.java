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


package io.fireflysoftware.core.banking.psdx.models.entities;

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Third Party Provider (TPP) that can access
 * customer data or perform operations on their behalf through PSD2/PSD3 APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("third_party_providers")
public class ThirdPartyProvider {

    @Id
    private UUID id;

    @Column("name")
    private String name;

    @Column("registration_number")
    private String registrationNumber;

    @Column("api_key")
    private String apiKey;

    @Column("redirect_uri")
    private String redirectUri;

    @Column("status")
    private ProviderStatus status;

    @Column("provider_type")
    private ProviderType providerType;

    @Column("national_competent_authority")
    private String nationalCompetentAuthority;

    @Column("national_competent_authority_country")
    private String nationalCompetentAuthorityCountry;

    @Column("roles")
    private String roles;

    @Column("certificate_serial_number")
    private String certificateSerialNumber;

    @Column("certificate_subject")
    private String certificateSubject;

    @Column("certificate_issuer")
    private String certificateIssuer;

    @Column("certificate_valid_from")
    private LocalDateTime certificateValidFrom;

    @Column("certificate_valid_until")
    private LocalDateTime certificateValidUntil;

    @Column("certificate_content")
    private String certificateContent;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
