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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
    void liveIdentityPropertyStillUsesTheSandboxAndOpensNoSocket() {
        new ApplicationContextRunner()
                .withBean(PasswordEncoder.class, BCryptPasswordEncoder::new)
                .withUserConfiguration(SandboxUserDetailsService.class)
                .withPropertyValues(
                        "integration.identity.enabled=true",
                        "integration.identity.base-url=http://core-common-user-mgmt:8080")
                .run(context -> {
                    assertThat(context).hasSingleBean(StaffIdentityPort.class);
                    assertThat(context.getBean(StaffIdentityPort.class)).isInstanceOf(SandboxUserDetailsService.class);
                    StepVerifier.create(context.getBean(StaffIdentityPort.class).authenticate("admin", "sandbox"))
                            .expectComplete()
                            .verify(Duration.ofSeconds(2));
                });
    }
}
