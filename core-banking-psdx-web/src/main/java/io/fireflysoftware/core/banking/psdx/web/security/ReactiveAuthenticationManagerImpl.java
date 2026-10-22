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


package io.fireflysoftware.core.banking.psdx.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Staff sign-in against the sandbox identity adapter. This core does not call another service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveAuthenticationManagerImpl implements ReactiveAuthenticationManager {

    private final StaffIdentityPort identity;

    /**
     * Authenticate a user.
     *
     * @param authentication The authentication object
     * @return A Mono of Authentication
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        String password = credentials == null ? "" : credentials.toString();

        log.debug("Authenticating user: {}", username);

        return identity.authenticate(username, password)
                .map(this::createAuthentication)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")));
    }

    /**
     * Create an authentication object from user details.
     *
     * @param userDetails The user details
     * @return The authentication object
     */
    private Authentication createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
