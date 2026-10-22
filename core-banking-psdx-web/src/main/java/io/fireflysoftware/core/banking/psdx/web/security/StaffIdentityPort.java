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

import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * Staff sign-in for the PSD2 service.
 * {@link SandboxUserDetailsService} is active while {@code integration.identity.enabled} is unset.
 * {@link SdkStaffIdentityAdapter} is the live bean. It looks up the account through
 * core-common-user-mgmt, which publishes no password. Sign-in then succeeds only when
 * {@link HttpPasswordVerifier} is configured. An installer replaces {@code HttpPasswordVerifier}.
 */
public interface StaffIdentityPort {

    Mono<UserDetails> authenticate(String username, String password);
}
