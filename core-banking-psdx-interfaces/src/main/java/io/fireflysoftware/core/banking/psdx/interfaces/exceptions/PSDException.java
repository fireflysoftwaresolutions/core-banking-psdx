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


package io.fireflysoftware.core.banking.psdx.interfaces.exceptions;

import lombok.Getter;

/**
 * Base exception for PSD2/PSD3 specific errors.
 */
@Getter
public class PSDException extends RuntimeException {

    private final String errorCode;
    private final String tppMessage;

    /**
     * Constructor for PSDException.
     *
     * @param errorCode The error code
     * @param message The error message
     */
    public PSDException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.tppMessage = null;
    }

    /**
     * Constructor for PSDException.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param tppMessage The TPP message
     */
    public PSDException(String errorCode, String message, String tppMessage) {
        super(message);
        this.errorCode = errorCode;
        this.tppMessage = tppMessage;
    }

    /**
     * Constructor for PSDException.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param cause The cause
     */
    public PSDException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.tppMessage = null;
    }

    /**
     * Constructor for PSDException.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param tppMessage The TPP message
     * @param cause The cause
     */
    public PSDException(String errorCode, String message, String tppMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.tppMessage = tppMessage;
    }
}
