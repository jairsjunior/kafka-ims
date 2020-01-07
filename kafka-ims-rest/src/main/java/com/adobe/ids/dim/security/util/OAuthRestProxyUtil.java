package com.adobe.ids.dim.security.util;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.entity.RequestInfo;
import com.adobe.ids.dim.security.metrics.OAuthMetrics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafkarest.resources.v2.ConsumersResource;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class OAuthRestProxyUtil {

    private static final Logger log = LoggerFactory.getLogger(OAuthRestProxyUtil.class);
    private static final String AUTHENTICATION_PREFIX = "Bearer";

    public static IMSBearerTokenJwt getIMSBearerTokenJwtFromBearer(String accessToken) throws IMSRestException {
        IMSBearerTokenJwt token = null;
        try {
            // Get client_id from the token
            String[] tokenString = accessToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payLoad = new String(decoder.decode(tokenString[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            Map < String, Object > payloadJson = objectMapper.readValue(payLoad, new TypeReference<Map<String, Object>>(){});
            token = new IMSBearerTokenJwt(payloadJson, accessToken);
        } catch (IOException e) {
            log.info("Cannot parse the token. Invalid Token sent!");
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            throw new IMSRestException(IMSRestException.BEARER_INVALID_TOKEN_CODE, IMSRestException.BEARER_INVALID_TOKEN_MSG);
        }
        return token;
    }

    public static boolean validateExpiration(IMSBearerTokenJwt token) {
        return token.lifetimeMs() > Time.SYSTEM.milliseconds();
    }

    public static boolean validateRequiredScope(IMSBearerTokenJwt token, String requiredScope){
        if(requiredScope == null){
            throw new IMSRestException(IMSRestException.BEARER_INVALID_TOKEN_CODE, IMSRestException.BEARER_INVALID_TOKEN_MSG);
        }
        return token.scope().contains(requiredScope);
    }

    public static IMSBearerTokenJwt getBearerInformation(ContainerRequestContext containerRequestContext) throws IMSRestException{
        String authorizationHeader = containerRequestContext.getHeaderString("Authorization");
        if(authorizationHeader == null){
            log.info("Authorization token is null");
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            throw new IMSRestException(IMSRestException.BEARER_TOKEN_NOT_SENT_CODE, IMSRestException.BEARER_TOKEN_NOT_SENT_MSG);
        }
        if (authorizationHeader.startsWith(AUTHENTICATION_PREFIX)) {
            String bearer = authorizationHeader.substring(AUTHENTICATION_PREFIX.length()).trim();
            return OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(bearer);
        }else{
            log.info("Invalid Token sent!");
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            throw new IMSRestException(IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_CODE, IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_MSG + AUTHENTICATION_PREFIX);
        }
    }

    public static String getResourceType(final ContainerRequestContext requestContext, ResourceInfo resourceInfo) {
        log.debug("getResourceType");
        if (ConsumersResource.class.equals(resourceInfo.getResourceClass()) || io.confluent.kafkarest.resources.ConsumersResource.class.equals(resourceInfo.getResourceClass())) {
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
        return path[path.length-1];
    }

    public static RequestInfo mountResquestInfo(ContainerRequestContext context, ResourceInfo resourceInfo) {
        String requestType = getResourceType(context, resourceInfo);
        String topic = "";
        if(requestType.equalsIgnoreCase("producer")){
            topic = getTopicProducer(context);
        }
        return new RequestInfo(requestType, topic);
    }
}
