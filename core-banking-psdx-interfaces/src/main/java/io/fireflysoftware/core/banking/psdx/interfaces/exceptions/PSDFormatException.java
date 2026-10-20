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

/**
 * Exception for format errors.
 */
public class PSDFormatException extends PSDException {

    private static final String ERROR_CODE = "FORMAT_ERROR";

    /**
     * Constructor for PSDFormatException.
     *
     * @param message The error message
     */
    public PSDFormatException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructor for PSDFormatException.
     *
     * @param message The error message
     * @param tppMessage The TPP message
     */
    public PSDFormatException(String message, String tppMessage) {
        super(ERROR_CODE, message, tppMessage);
    }

    /**
     * Constructor for PSDFormatException.
     *
     * @param message The error message
     * @param cause The cause
     */
    public PSDFormatException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructor for PSDFormatException.
     *
     * @param message The error message
     * @param tppMessage The TPP message
     * @param cause The cause
     */
    public PSDFormatException(String message, String tppMessage, Throwable cause) {
        super(ERROR_CODE, message, tppMessage, cause);
    }
}
