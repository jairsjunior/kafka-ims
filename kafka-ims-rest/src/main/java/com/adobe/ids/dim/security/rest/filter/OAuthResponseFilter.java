package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.common.exception.IMSValidatorException;
import com.adobe.ids.dim.security.entity.RequestInfo;
import com.adobe.ids.dim.security.metrics.IMSRestMetrics;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
import io.confluent.kafkarest.entities.PartitionOffset;
import io.confluent.kafkarest.entities.ProduceResponse;
import io.confluent.rest.entities.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class OAuthResponseFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthResponseFilter.class);

    @Context ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        log.debug("Status Response: " + containerResponseContext.getStatus());
        if (containerResponseContext.hasEntity()) {
            log.debug("Entity Response: " + containerResponseContext.getEntity().toString());
        }

        if (containerResponseContext.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            IMSRestMetrics.getInstance().incSucessfull(containerRequestContext, resourceInfo);
        } else {
            switch (containerResponseContext.getStatus()) {
            case 401: {
                log.debug("Authentication error");
                handleAuthenticationErrorCodeResponse(containerRequestContext, containerResponseContext);
            }
            break;
            case 403: {
                log.debug("Authorization error");
                handleAuthorizationErrorCodeResponse(containerRequestContext, containerResponseContext);
            }
            default: {
                log.error("Authentication/Authorization error:");
                log.error("statusCode=" + containerResponseContext.getStatus());
                log.error("responseObject=" + containerResponseContext.getEntity().toString());
            }
            }
        }
    }

    private void cleanContextOfPrincipal(ContainerRequestContext context) {
        try{
            IMSBearerTokenJwt token = OAuthRestProxyUtil.getBearerInformation(context, resourceInfo, false);
            KafkaOAuthRestContextFactory.getInstance().cleanSpecificContext(token.principalName());
        }catch (IMSRestException e){
            log.error("Could not clean context of principal: ", e);
        }
    }

    private void generateMetricsForProducerWithoutAuthorization(ContainerRequestContext context) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, null);
        IMSRestMetrics.getInstance().incACLDenied(context, resourceInfo, req.getTopics());
    }

    private void generateMetricsForErrorWithoutAuthorization(ContainerRequestContext context, ErrorMessage error) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, error);
        IMSRestMetrics.getInstance().incACLDenied(context, resourceInfo, req.getTopics());
    }

    private void handleAuthenticationErrorCodeResponse(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext){
        log.debug("Authentication error - Clearing this Principal Context");
        cleanContextOfPrincipal(containerRequestContext);
        ErrorMessage error = null;
        if (ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
            ProduceResponse produceResponse = (ProduceResponse) containerResponseContext.getEntity();
            log.debug("Authentication 401 -- Producer Response");
            log.debug(produceResponse.toString());
            if(!produceResponse.getOffsets().isEmpty() && produceResponse.getOffsets().get(0).getError() != null){
                PartitionOffset partitionOffset = produceResponse.getOffsets().get(0);
                error = new ErrorMessage(partitionOffset.getErrorCode(), partitionOffset.getError());
            }else{
                log.error("Authentication 401 -- Producer Response is not Empty, but the first element is not an error");
                return;
            }
        } else if (ErrorMessage.class.isInstance(containerResponseContext.getEntity())) {
            error = (ErrorMessage) containerResponseContext.getEntity();
        } else {
            log.error("Not mapped Class: " + containerResponseContext.getEntityClass().toString());
            return;
        }

        if (IMSValidatorException.KAFKA_EXCEPTION_WITHOUT_SCOPE_MSG.indexOf(error.getMessage()) > -1) {
            // Needed to change the error response to IMSRestException on Kafka Rest
            // and Collect Without Scope Metrics
            IMSRestMetrics.getInstance().incWithoutScope(containerRequestContext, resourceInfo);
            throw new IMSRestException(error.getErrorCode(), error.getMessage());
        } else if (IMSRestException.BEARER_TOKEN_EXPIRED_MSG.indexOf(error.getMessage()) > -1) {
            IMSRestMetrics.getInstance().incExpiredToken(containerRequestContext, resourceInfo);
        }
    }

    private void handleAuthorizationErrorCodeResponse(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext){
        if (ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
            log.debug("Authorization 403 -- Producer Response");
            generateMetricsForProducerWithoutAuthorization(containerRequestContext);
            //Needed to change the error from ProduceResponse to IMSRestException response on Kafka Rest
            ProduceResponse produceResponse = (ProduceResponse) containerResponseContext.getEntity();
            if(!produceResponse.getOffsets().isEmpty() && produceResponse.getOffsets().get(0).getError() != null){
                throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), produceResponse.getOffsets().get(0).getError());
            }else{
                log.error("Authorization 403 -- Producer Response is not Empty, but the first element is not an error");
            }
        } else if (ErrorMessage.class.isInstance(containerResponseContext.getEntity())) {
            generateMetricsForErrorWithoutAuthorization(
                    containerRequestContext, (ErrorMessage) containerResponseContext.getEntity());
        } else {
            log.debug("Not mapped Class: " + containerResponseContext.getEntityClass().toString());
        }
    }
}
