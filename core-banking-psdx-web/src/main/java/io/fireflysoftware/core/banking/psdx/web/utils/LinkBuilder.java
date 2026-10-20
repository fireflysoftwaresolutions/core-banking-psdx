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


package io.fireflysoftware.core.banking.psdx.web.utils;

import io.fireflysoftware.core.banking.psdx.interfaces.dtos.PSDLinksDTO;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building HATEOAS links.
 */
public class LinkBuilder {

    private final UriComponentsBuilder baseBuilder;
    private final Map<String, PSDLinksDTO.PSDLinkDTO> additionalLinks = new HashMap<>();
    private PSDLinksDTO.PSDLinkDTO self;
    private PSDLinksDTO.PSDLinkDTO first;
    private PSDLinksDTO.PSDLinkDTO prev;
    private PSDLinksDTO.PSDLinkDTO next;
    private PSDLinksDTO.PSDLinkDTO last;
    private PSDLinksDTO.PSDLinkDTO status;
    private PSDLinksDTO.PSDLinkDTO account;
    private PSDLinksDTO.PSDLinkDTO balances;
    private PSDLinksDTO.PSDLinkDTO transactions;
    private PSDLinksDTO.PSDLinkDTO scaRedirect;
    private PSDLinksDTO.PSDLinkDTO scaStatus;
    private PSDLinksDTO.PSDLinkDTO confirmation;
    private PSDLinksDTO.PSDLinkDTO authorization;

    /**
     * Create a new LinkBuilder with the given base URL.
     *
     * @param baseUrl The base URL
     * @return A new LinkBuilder
     */
    public static LinkBuilder fromBaseUrl(String baseUrl) {
        return new LinkBuilder(UriComponentsBuilder.fromUriString(baseUrl));
    }

    /**
     * Create a new LinkBuilder with the given UriComponentsBuilder.
     *
     * @param baseBuilder The UriComponentsBuilder
     */
    private LinkBuilder(UriComponentsBuilder baseBuilder) {
        this.baseBuilder = baseBuilder;
    }

    /**
     * Add a self link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withSelf(String path) {
        this.self = createLink(path);
        return this;
    }

    /**
     * Add a first link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withFirst(String path) {
        this.first = createLink(path);
        return this;
    }

    /**
     * Add a previous link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withPrev(String path) {
        this.prev = createLink(path);
        return this;
    }

    /**
     * Add a next link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withNext(String path) {
        this.next = createLink(path);
        return this;
    }

    /**
     * Add a last link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withLast(String path) {
        this.last = createLink(path);
        return this;
    }

    /**
     * Add a status link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withStatus(String path) {
        this.status = createLink(path);
        return this;
    }

    /**
     * Add an account link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withAccount(String path) {
        this.account = createLink(path);
        return this;
    }

    /**
     * Add a balances link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withBalances(String path) {
        this.balances = createLink(path);
        return this;
    }

    /**
     * Add a transactions link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withTransactions(String path) {
        this.transactions = createLink(path);
        return this;
    }

    /**
     * Add an SCA redirect link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withScaRedirect(String path) {
        this.scaRedirect = createLink(path);
        return this;
    }

    /**
     * Add an SCA status link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withScaStatus(String path) {
        this.scaStatus = createLink(path);
        return this;
    }

    /**
     * Add a confirmation link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withConfirmation(String path) {
        this.confirmation = createLink(path);
        return this;
    }

    /**
     * Add an authorization link.
     *
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withAuthorization(String path) {
        this.authorization = createLink(path);
        return this;
    }

    /**
     * Add an additional link.
     *
     * @param name The name of the link
     * @param path The path
     * @return This LinkBuilder
     */
    public LinkBuilder withAdditionalLink(String name, String path) {
        this.additionalLinks.put(name, createLink(path));
        return this;
    }

    /**
     * Create a link with the given path.
     *
     * @param path The path
     * @return The link
     */
    private PSDLinksDTO.PSDLinkDTO createLink(String path) {
        String url = baseBuilder.cloneBuilder().path(path).build().toUriString();
        return PSDLinksDTO.PSDLinkDTO.builder().href(url).build();
    }

    /**
     * Build the links.
     *
     * @return The links
     */
    public PSDLinksDTO build() {
        return PSDLinksDTO.builder()
                .self(self)
                .first(first)
                .prev(prev)
                .next(next)
                .last(last)
                .status(status)
                .account(account)
                .balances(balances)
                .transactions(transactions)
                .scaRedirect(scaRedirect)
                .scaStatus(scaStatus)
                .confirmation(confirmation)
                .authorization(authorization)
                .additionalLinks(additionalLinks.isEmpty() ? null : additionalLinks)
                .build();
    }
}
