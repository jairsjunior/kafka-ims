/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.common.exception;

import org.apache.kafka.common.errors.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMSValidatorException extends AuthorizationException {

    public static String KAFKA_EXCEPTION_WITHOUT_SCOPE_MSG =
        "Token doesn't have required scopes! We cannot accept this token. Please work with DIM team to get needed scopes added.";
    private final Logger log = LoggerFactory.getLogger(IMSValidatorException.class);

    public IMSValidatorException(String message) {
        super(message);
        log.error("IMS Validator Exception: " + message);
    }
}
