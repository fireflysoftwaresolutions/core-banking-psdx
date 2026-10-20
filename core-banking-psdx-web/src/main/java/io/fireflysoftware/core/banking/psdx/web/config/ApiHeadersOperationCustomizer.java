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


package io.fireflysoftware.core.banking.psdx.web.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;

/**
 * Customizer for OpenAPI operations to add common headers.
 */
@Component
public class ApiHeadersOperationCustomizer implements OperationCustomizer {

    /**
     * Customize the OpenAPI operation by adding common headers.
     *
     * @param operation The OpenAPI operation
     * @param handlerMethod The handler method
     * @return The customized OpenAPI operation
     */
    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getParameters() == null) {
            operation.setParameters(new ArrayList<>());
        }
        
        // Add common headers to all operations
        addHeaderIfNotExists(operation, "X-Request-ID", "Unique ID for the request", "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
        
        // Add consent header to all operations except authentication and TPP registration
        if (!operation.getOperationId().contains("authenticate") && 
            !operation.getOperationId().contains("registerProvider") &&
            !operation.getOperationId().contains("validateApiKey")) {
            addHeaderIfNotExists(operation, "X-Consent-ID", "ID of the consent for the operation", "12345");
        }
        
        // Add PSU headers to account and payment operations
        if (operation.getOperationId().contains("Account") || 
            operation.getOperationId().contains("Payment") ||
            operation.getOperationId().contains("Transaction") ||
            operation.getOperationId().contains("Balance")) {
            addHeaderIfNotExists(operation, "PSU-ID", "ID of the customer", "user@example.com");
            addHeaderIfNotExists(operation, "PSU-IP-Address", "IP address of the customer", "192.168.1.1");
        }
        
        return operation;
    }

    /**
     * Add a header parameter to the operation if it doesn't already exist.
     *
     * @param operation The OpenAPI operation
     * @param name The name of the header
     * @param description The description of the header
     * @param example An example value for the header
     */
    private void addHeaderIfNotExists(Operation operation, String name, String description, String example) {
        boolean headerExists = operation.getParameters().stream()
                .anyMatch(parameter -> "header".equals(parameter.getIn()) && name.equals(parameter.getName()));
        
        if (!headerExists) {
            Parameter parameter = new Parameter()
                    .in("header")
                    .name(name)
                    .description(description)
                    .example(example)
                    .schema(new io.swagger.v3.oas.models.media.StringSchema());
            
            operation.getParameters().add(parameter);
        }
    }
}
