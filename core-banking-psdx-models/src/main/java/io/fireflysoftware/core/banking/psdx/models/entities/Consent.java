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

import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentStatus;
import io.fireflysoftware.core.banking.psdx.interfaces.enums.ConsentType;
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
 * Entity representing a consent given by a customer for accessing their data
 * or performing operations on their behalf according to PSD2/PSD3 regulations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("consents")
public class Consent {

    @Id
    private UUID id;

    @Column("party_id")
    private UUID partyId;

    @Column("consent_type")
    private ConsentType consentType;

    @Column("status")
    private ConsentStatus status;

    @Column("valid_from")
    private LocalDateTime validFrom;

    @Column("valid_until")
    private LocalDateTime validUntil;

    @Column("access_frequency")
    private Integer accessFrequency;

    @Column("access_scope")
    private String accessScope;

    @Column("last_action_date")
    private LocalDateTime lastActionDate;

    @Column("access")
    private String access;

    @Column("combined_service_indicator")
    private Boolean combinedServiceIndicator;

    @Column("recurring_indicator")
    private Boolean recurringIndicator;

    @Column("frequency_per_day")
    private Integer frequencyPerDay;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
