/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class StringsUtil {

    private static final Logger log = LoggerFactory.getLogger(StringsUtil.class);

    public static boolean isNullOrEmpty(String s) {
        if (s == null) {
            return true;
        }
        if (s.isEmpty()) {
            return true;
        }
        return false;
    }

    public static Map<String, Object> jsonStringToMap(String string) {
        Map<String, Object> result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.readValue(string, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            log.error("Error at convert jsonString to Map");
        }
        return result;
    }
}
