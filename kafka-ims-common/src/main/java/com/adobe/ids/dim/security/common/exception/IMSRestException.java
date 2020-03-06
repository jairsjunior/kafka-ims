/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.common.exception;

import io.confluent.rest.exceptions.RestNotAuthorizedException;

public class IMSRestException extends RestNotAuthorizedException {

    public static String BEARER_TOKEN_NOT_SENT_MSG = "Authorization Bearer Token not sent";
    public static int BEARER_TOKEN_NOT_SENT_CODE = 401002;
    public static String BEARER_INVALID_TOKEN_MSG = "Invalid Token";
    public static int BEARER_INVALID_TOKEN_CODE = 401003;
    public static String BEARER_SENT_NOT_STARTING_WITH_PREFIX_MSG =
        "Authorization Bearer Token does not start Bearer";
    public static int BEARER_SENT_NOT_STARTING_WITH_PREFIX_CODE = 401004;
    public static String BEARER_TOKEN_EXPIRED_MSG = "Bearer token has expired";
    public static int BEARER_TOKEN_EXPIRED_CODE = 401005;
    public static String BEARER_IS_NOT_INSTANCE_IMS_JWT_MSG =
        "Principal is not a instance of IMSBearerTokenJwt";
    public static int BEARER_IS_NOT_INSTANCE_IMS_JWT_CODE = 401006;
    public static String REST_CONFIGURATION_ERROR_MSG = "Problem creating REST config object: ";
    public static int REST_CONFIGURATION_ERROR_CODE = 401007;
    public static String JWT_WITHOUT_SCOPE_ERROR_MSG = "Token doesn't have required scopes! We cannot accept this token. Please work with DIM team to get needed scopes added.";
    public static int JWT_WITHOUT_SCOPE_ERROR_CODE = 401001;


    public IMSRestException(int errCode, String message) {
        super("Error Code: " + String.valueOf(errCode) + ", Error Message: " + message, errCode);
    }
}
