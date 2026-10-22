/*
 * Copyright 2026 Firefly Software Foundation
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


package io.fireflysoftware.core.banking.psdx.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.fireflysoftware.core.banking.psdx.core.adapters.SdkCalls;
import io.fireflysoftware.core.users.sdk.api.UserAccountsApi;
import io.fireflysoftware.core.users.sdk.invoker.ApiClient;
import io.fireflysoftware.core.users.sdk.model.FilterRequestUserAccountDTO;
import io.fireflysoftware.core.users.sdk.model.PaginationRequest;
import io.fireflysoftware.core.users.sdk.model.PaginationResponse;
import io.fireflysoftware.core.users.sdk.model.UserAccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Looks up a staff account through {@code UserAccountsApi}. core-common-user-mgmt publishes
 * no password, so {@link #authenticate} succeeds only when a {@link PasswordVerifier} is present.
 * Selected when {@code integration.identity.enabled=true}. The account base URL is
 * {@code integration.identity.base-url}. An installer replaces {@link HttpPasswordVerifier}.
 */
@Component
@ConditionalOnProperty(name = "integration.identity.enabled", havingValue = "true")
public final class SdkStaffIdentityAdapter implements StaffIdentityPort, ReactiveUserDetailsService {

    static final String NO_PUBLISHED_PASSWORD = "{unusable}core-common-user-mgmt-publishes-no-password";

    private static final ObjectMapper JSON = new ObjectMapper().registerModule(new JavaTimeModule());

    private final UserAccountsApi accounts;
    private final PasswordVerifier verifier;

    @Autowired
    public SdkStaffIdentityAdapter(
            @Value("${integration.identity.base-url}") String baseUrl,
            @Value("${integration.identity.verifier-url:}") String verifierUrl) {
        ApiClient client = new ApiClient();
        client.setBasePath(SdkCalls.base(baseUrl));
        this.accounts = new UserAccountsApi(client);
        this.verifier = verifierUrl == null || verifierUrl.isBlank() ? null : new HttpPasswordVerifier(verifierUrl);
    }

    SdkStaffIdentityAdapter(UserAccountsApi accounts, PasswordVerifier verifier) {
        this.accounts = accounts;
        this.verifier = verifier;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return lookup(username).map(this::details);
    }

    @Override
    public Mono<UserDetails> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Mono.empty();
        }
        return lookup(username).flatMap(account -> {
            if (verifier == null) {
                return Mono.empty();
            }
            return verifier.matches(username, password)
                    .onErrorReturn(false)
                    .filter(Boolean::booleanValue)
                    .map(matched -> details(account));
        });
    }

    private Mono<UserAccountDTO> lookup(String username) {
        if (username == null || username.isBlank()) {
            return Mono.empty();
        }
        Optional<UUID> id = parse(username);
        Mono<UserAccountDTO> found = id.isPresent()
                ? emptyIfMissing(accounts.getUserAccountById(id.get(), key()))
                : byEmail(username);
        return found.filter(SdkStaffIdentityAdapter::usable);
    }

    private Mono<UserAccountDTO> byEmail(String email) {
        UserAccountDTO filter = new UserAccountDTO();
        filter.setEmail(email);
        PaginationRequest pagination = new PaginationRequest();
        pagination.setPageNumber(0);
        pagination.setPageSize(5);
        FilterRequestUserAccountDTO request = new FilterRequestUserAccountDTO();
        request.setFilters(filter);
        request.setPagination(pagination);
        return emptyIfMissing(accounts.filterUserAccounts(request, key()))
                .flatMap(response -> matchEmail(response, email));
    }

    private static Mono<UserAccountDTO> matchEmail(PaginationResponse response, String email) {
        if (response == null) {
            return Mono.empty();
        }
        return rows(response.getContent()).stream()
                .filter(row -> email.equalsIgnoreCase(row.getEmail()))
                .findFirst()
                .map(Mono::just)
                .orElseGet(Mono::empty);
    }

    private UserDetails details(UserAccountDTO account) {
        String name = account.getEmail() == null || account.getEmail().isBlank()
                ? account.getId().toString()
                : account.getEmail();
        return User.withUsername(name)
                .password(NO_PUBLISHED_PASSWORD)
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
    }

    private static boolean usable(UserAccountDTO row) {
        return row != null
                && Boolean.TRUE.equals(row.getIsActive())
                && (row.getId() != null || (row.getEmail() != null && !row.getEmail().isBlank()));
    }

    private static Optional<UUID> parse(String username) {
        try {
            return Optional.of(UUID.fromString(username));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }

    private static <T> Mono<T> emptyIfMissing(Mono<T> call) {
        return call.onErrorResume(SdkStaffIdentityAdapter::missing, error -> Mono.empty());
    }

    private static boolean missing(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof WebClientResponseException http && http.getStatusCode().value() == 404) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static List<UserAccountDTO> rows(List<?> content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        List<UserAccountDTO> rows = new ArrayList<>();
        for (Object item : content) {
            if (item == null) {
                continue;
            }
            if (item instanceof UserAccountDTO dto) {
                rows.add(dto);
            } else {
                rows.add(JSON.convertValue(item, UserAccountDTO.class));
            }
        }
        return rows;
    }
}
