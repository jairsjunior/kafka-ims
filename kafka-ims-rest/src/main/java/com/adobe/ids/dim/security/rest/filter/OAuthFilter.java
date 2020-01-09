package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.metrics.IMSRestMetrics;
import com.adobe.ids.dim.security.util.*;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import io.confluent.kafkarest.resources.v2.ConsumersResource;
import io.confluent.rest.RestConfigException;
import io.confluent.rest.exceptions.RestNotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Priority(1000)
public class OAuthFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthFilter.class);
    private final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig;

    @Context
    ResourceInfo resourceInfo;

    public OAuthFilter(final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig) {
        log.debug("Constructor of OAuthFilter");
        this.oauthSecurityRestConfig = oauthSecurityRestConfig;
    }

    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        log.debug("Filter of OAuthFilter");
        if (containerRequestContext.getSecurityContext() != null) {
            final String resourceType = OAuthRestProxyUtil.getResourceType(containerRequestContext, resourceInfo);
            log.debug("ResourceType: " + resourceType);
            final IMSBearerTokenJwt principal = OAuthRestProxyUtil.getBearerInformation(containerRequestContext, resourceInfo);
            log.debug("Principal: " + principal.toString());
            final KafkaRestContext context = this.getKafkaRestContext(resourceType, principal, containerRequestContext);
            log.debug("Context: " + context.toString());
            KafkaRestContextProvider.setCurrentContext(context);
        }
    }

    private KafkaRestContext getKafkaRestContext(final String resourceType, final IMSBearerTokenJwt principal, ContainerRequestContext containerRequestContext) throws IMSRestException {
        log.debug("getKafkaRestContext");
        final KafkaRestContext context;
        final KafkaOAuthSecurityRestConfig bearerTokenKafkaRestConfig;
        if (principal instanceof IMSBearerTokenJwt) {
            log.debug("principal is instance of IMSBearerTokenJwt");
            if(!OAuthRestProxyUtil.validateExpiration(principal)) {
                log.info("Bearer token has expired!");
                IMSRestMetrics.getInstance().incExpiredToken(containerRequestContext, resourceInfo);
                KafkaOAuthRestContextFactory.getInstance().cleanSpecificContext(principal.principalName());
                throw new IMSRestException(IMSRestException.BEARER_TOKEN_EXPIRED_CODE, IMSRestException.BEARER_TOKEN_EXPIRED_MSG);
            }
            try {
                log.debug("create of bearerTokenKafkaRestConfig");
                bearerTokenKafkaRestConfig = new KafkaOAuthSecurityRestConfig(this.oauthSecurityRestConfig.getOriginalProperties(), principal);
            }
            catch (RestConfigException e) {
                log.debug("RestConfigException");
                throw new IMSRestException(IMSRestException.REST_CONFIGURATION_ERROR_CODE, IMSRestException.REST_CONFIGURATION_ERROR_MSG + e.getMessage());
            }
            log.debug("Get context using Factory");
            context = KafkaOAuthRestContextFactory.getInstance().getContext(principal, bearerTokenKafkaRestConfig, resourceType, true);
        } else {
            log.info("Invalid token!");
            IMSRestMetrics.getInstance().incInvalidToken(containerRequestContext, resourceInfo);
            log.debug("Principal is not a instance of IMSBearerTokenJwt");
            throw new IMSRestException(IMSRestException.BEARER_IS_NOT_INSTANCE_IMS_JWT_CODE, IMSRestException.BEARER_IS_NOT_INSTANCE_IMS_JWT_MSG);
        }
        log.debug("context: " + context.toString());
        return context;
    }


}
