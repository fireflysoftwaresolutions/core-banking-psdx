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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a funds confirmation request/response according to PSD2/PSD3 standards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("funds_confirmations")
public class FundsConfirmation {

    @Id
    private UUID id;

    @Column("consent_id")
    private UUID consentId;

    @Column("account_reference")
    private String accountReference;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("creditor_name")
    private String creditorName;

    @Column("creditor_account")
    private String creditorAccount;

    @Column("card_number")
    private String cardNumber;

    @Column("psu_name")
    private String psuName;

    @Column("funds_available")
    private Boolean fundsAvailable;

    @Column("confirmation_date_time")
    private LocalDateTime confirmationDateTime;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
}