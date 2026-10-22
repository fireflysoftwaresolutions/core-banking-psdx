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

import io.fireflysoftware.core.users.sdk.api.UserAccountsApi;
import io.fireflysoftware.core.users.sdk.model.PaginationResponse;
import io.fireflysoftware.core.users.sdk.model.UserAccountDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SandboxUserDetailsServiceTest {

    @Test
    void sandboxAcceptsTheDeterministicUsers() {
        var sandbox = new SandboxUserDetailsService(new BCryptPasswordEncoder());
        var manager = new ReactiveAuthenticationManagerImpl(sandbox);

        StepVerifier.create(sandbox.authenticate("admin", "admin"))
                .assertNext(user -> assertThat(user.getUsername()).isEqualTo("admin"))
                .verifyComplete();
        StepVerifier.create(manager.authenticate(new UsernamePasswordAuthenticationToken("user", "user")))
                .assertNext(authentication -> assertThat(authentication.isAuthenticated()).isTrue())
                .verifyComplete();
    }

    @Test
    void sandboxRejectsUnknownPasswords() {
        var sandbox = new SandboxUserDetailsService(new BCryptPasswordEncoder());
        var manager = new ReactiveAuthenticationManagerImpl(sandbox);

        StepVerifier.create(sandbox.authenticate("admin", "sandbox"))
                .expectComplete()
                .verify(Duration.ofSeconds(2));
        StepVerifier.create(sandbox.authenticate("user", "123456"))
                .expectComplete()
                .verify(Duration.ofSeconds(2));
        StepVerifier.create(manager.authenticate(new UsernamePasswordAuthenticationToken("user", "sandbox")))
                .expectError(BadCredentialsException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void liveLookupDoesNotAuthenticateWithoutAVerifier() {
        UserAccountDTO account = account();
        UserAccountsApi api = accountsReturning(account);
        var adapter = new SdkStaffIdentityAdapter(api, null);

        StepVerifier.create(adapter.authenticate("ada@example.com", "secret"))
                .expectComplete()
                .verify(Duration.ofSeconds(2));
        verify(api).filterUserAccounts(any(), any());
        assertThat(adapter.findByUsername("ada@example.com").block(Duration.ofSeconds(2)).getPassword())
                .isEqualTo(SdkStaffIdentityAdapter.NO_PUBLISHED_PASSWORD);
    }

    @Test
    void liveAdapterAuthenticatesOnlyThroughTheConfiguredVerifier() {
        UserAccountDTO account = account();
        UserAccountsApi api = accountsReturning(account);
        PasswordVerifier verifier = (username, password) -> Mono.just("secret".equals(password));
        var adapter = new SdkStaffIdentityAdapter(api, verifier);

        var accepted = adapter.authenticate("ada@example.com", "secret").block(Duration.ofSeconds(2));
        assertThat(accepted.getUsername()).isEqualTo("ada@example.com");
        assertThat(accepted.getPassword()).isEqualTo(SdkStaffIdentityAdapter.NO_PUBLISHED_PASSWORD);
        assertThat(accepted.getPassword()).doesNotContain("secret");

        StepVerifier.create(adapter.authenticate("ada@example.com", "other"))
                .expectComplete()
                .verify(Duration.ofSeconds(2));

        AtomicReference<String> seen = new AtomicReference<>();
        WebClient client = WebClient.builder()
                .baseUrl("http://verifier.internal/check")
                .exchangeFunction(request -> {
                    seen.set(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
                })
                .build();
        assertThat(new HttpPasswordVerifier(client).matches("ada@example.com", "secret").block(Duration.ofSeconds(2)))
                .isTrue();
        assertThat(seen.get()).isEqualTo("http://verifier.internal/check");
        assertThatThrownBy(() -> new HttpPasswordVerifier(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verifier-url");
    }

    private static UserAccountDTO account() {
        UserAccountDTO account = new UserAccountDTO(UUID.fromString("11111111-1111-4111-8111-111111111111"));
        account.setEmail("ada@example.com");
        account.setIsActive(true);
        return account;
    }

    private static UserAccountsApi accountsReturning(UserAccountDTO account) {
        java.util.List<Object> content = new java.util.ArrayList<>();
        content.add(account);
        PaginationResponse page = new PaginationResponse();
        page.setContent(content);
        UserAccountsApi api = mock(UserAccountsApi.class);
        when(api.filterUserAccounts(any(), any())).thenReturn(Mono.just(page));
        return api;
    }
}
