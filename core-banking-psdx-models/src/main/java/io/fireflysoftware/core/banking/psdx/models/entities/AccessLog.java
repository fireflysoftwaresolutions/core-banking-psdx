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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.AccessType;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a log of access to customer data or operations
 * performed on their behalf through the PSD2/PSD3 APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("access_logs")
public class AccessLog {

    @Id
    private UUID id;

    @Column("consent_id")
    private UUID consentId;

    @Column("party_id")
    private UUID partyId;

    @Column("third_party_id")
    private String thirdPartyId;

    @Column("access_type")
    private AccessType accessType;

    @Column("resource_type")
    private ResourceType resourceType;

    @Column("resource_id")
    private String resourceId;

    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column("status")
    private AccessStatus status;

    @Column("error_message")
    private String errorMessage;

    @Column("x_request_id")
    private String xRequestId;

    @Column("tpp_request_id")
    private String tppRequestId;

    @Column("psu_id")
    private String psuId;

    @Column("psu_id_type")
    private String psuIdType;

    @Column("psu_corporate_id")
    private String psuCorporateId;

    @Column("psu_corporate_id_type")
    private String psuCorporateIdType;

    @Column("tpp_redirect_uri")
    private String tppRedirectUri;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("timestamp")
    private LocalDateTime timestamp;
}
