/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.util;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.entity.RequestInfo;
import com.adobe.ids.dim.security.metrics.IMSRestMetrics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafkarest.resources.v2.ConsumersResource;
import io.confluent.rest.entities.ErrorMessage;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthRestProxyUtil {

    private static final Logger log = LoggerFactory.getLogger(OAuthRestProxyUtil.class);
    private static final String AUTHENTICATION_PREFIX = "Bearer";

    public static OAuthRestProxyUtil util;
    private static Pattern pattern = Pattern.compile("\\[(.*)\\]");
    @Context ResourceInfo resourceInfoTest;


    public static OAuthRestProxyUtil getInstance() {
        if(util == null) {
            util = new OAuthRestProxyUtil();
        }
        return util;
    }

    public static IMSBearerTokenJwt getIMSBearerTokenJwtFromBearer(String accessToken)
    throws IMSRestException {
        IMSBearerTokenJwt token = null;
        try {
            // Get client_id from the token
            String[] tokenString = accessToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payLoad = new String(decoder.decode(tokenString[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            Map < String, Object > payloadJson = objectMapper.readValue(payLoad, new TypeReference<Map<String, Object>>() {});
            token = new IMSBearerTokenJwt(payloadJson, accessToken);
        } catch (Exception e) {
            log.debug("Cannot parse the token. Invalid Token sent!");
            throw new IMSRestException(
                IMSRestException.BEARER_INVALID_TOKEN_CODE, IMSRestException.BEARER_INVALID_TOKEN_MSG);
        }
        return token;
    }

    public static boolean validateExpiration(IMSBearerTokenJwt token) {
        return token.lifetimeMs() > Time.SYSTEM.milliseconds();
    }

    public static boolean validateRequiredScope(IMSBearerTokenJwt token, String requiredScope) {
        if(requiredScope == null) {
            throw new IMSRestException(IMSRestException.BEARER_INVALID_TOKEN_CODE, IMSRestException.BEARER_INVALID_TOKEN_MSG);
        }
        return token.scope().contains(requiredScope);
    }

    public static IMSBearerTokenJwt getBearerInformation(ContainerRequestContext containerRequestContext, ResourceInfo resourceInfo) throws IMSRestException {
        String authorizationHeader = containerRequestContext.getHeaderString("Authorization");
        if(authorizationHeader == null) {
            log.info("Authorization token is null");
            IMSRestMetrics.getInstance().incInvalidToken(containerRequestContext, resourceInfo);
            throw new IMSRestException(
                IMSRestException.BEARER_TOKEN_NOT_SENT_CODE, IMSRestException.BEARER_TOKEN_NOT_SENT_MSG);
        }
        if (authorizationHeader.startsWith(AUTHENTICATION_PREFIX)) {
            String bearer = authorizationHeader.substring(AUTHENTICATION_PREFIX.length()).trim();
            try {
                return OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(bearer);
            } catch (IMSRestException e) {
                log.debug("Invalid Token sent!");
                IMSRestMetrics.getInstance().incInvalidToken(containerRequestContext, resourceInfo);
                throw e;
            }
        }
        log.debug("Invalid Token sent!");
        IMSRestMetrics.getInstance().incInvalidToken(containerRequestContext, resourceInfo);
        throw new IMSRestException(
            IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_CODE,
            IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_MSG + AUTHENTICATION_PREFIX);
    }

    public static String getResourceType(
        final ContainerRequestContext requestContext, ResourceInfo resourceInfo) {
        log.debug("getResourceType");
        if (ConsumersResource.class.equals(resourceInfo.getResourceClass())
                || io.confluent.kafkarest.resources.ConsumersResource.class.equals(
                    resourceInfo.getResourceClass())) {
            log.debug("consumer");
            return "consumer";
        }
        if (requestContext.getMethod().equals("POST")) {
            log.debug("producer");
            return "producer";
        }
        log.debug("admin");
        return "admin";
    }

    public static String getTopicProducer(ContainerRequestContext context) {
        String[] path = context.getUriInfo().getPath().split("/");
        return path[path.length - 1];
    }

    public static List<String> extractTopicsFromErrors(String errorMessage) {
        List<String> results = new ArrayList<>();
        Matcher matcher = pattern.matcher(errorMessage);
        while (matcher.find()) {
            String result = matcher.group(1);
            if(result != null) {
                String[] topics = result.split(",");
                for(String s : topics) {
                    if(!s.isEmpty()) {
                        results.add(s.trim());
                    }
                }
                if(topics.length == 0) {
                    if(!result.isEmpty());
                    results.add(result.trim());
                }
            }
        }
        return results;
    }

    public static RequestInfo mountResquestInfo(
        ContainerRequestContext context, ResourceInfo resourceInfo, ErrorMessage error) {
        String requestType = getResourceType(context, resourceInfo);
        List<String> topic = new ArrayList<>();
        if(requestType.equalsIgnoreCase("producer")) {
            topic.add(getTopicProducer(context));
        } else if(error != null) {
            topic = extractTopicsFromErrors(error.getMessage());
        }
        return new RequestInfo(requestType, context.getUriInfo().getPath(), topic);
    }
}
