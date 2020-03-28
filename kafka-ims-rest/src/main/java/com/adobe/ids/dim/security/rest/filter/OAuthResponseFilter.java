package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.StringsUtil;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.entity.RequestInfo;
import com.adobe.ids.dim.security.metrics.IMSRestMetrics;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
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
import java.util.Map;

public class OAuthResponseFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthResponseFilter.class);

    @Context ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext){
        log.debug("Status Response: " + containerResponseContext.getStatus());
        if (containerResponseContext.hasEntity()) {
            log.debug("Entity Response: " + containerResponseContext.getEntity().toString());
        }

        if (containerResponseContext.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            IMSRestMetrics.getInstance().incSucessfull(containerRequestContext, resourceInfo);
        } else {
            switch (containerResponseContext.getStatus()) {
                case 401: {
                    log.debug("Authentication error - Clearing this Principal Context");
                    cleanContextOfPrincipal(containerRequestContext);
                    generateAuthenticationException(containerRequestContext, containerResponseContext);
                }
                break;
                case 403: {
                    log.debug("Authorization error! - Add JMX Metrics..");
                    if (ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
                        log.debug("403: Producer Response Class");
                        generateMetricsForProducerWithoutAuthorization(containerRequestContext);
                        ProduceResponse produceResponse = (ProduceResponse) containerResponseContext.getEntity();
                        if(!produceResponse.getOffsets().isEmpty()){
                            Map<String, Object> errorResponse = StringsUtil.jsonStringToMap(produceResponse.getOffsets().get(0).getError());
                            if (errorResponse != null) {
                                StringBuffer sbuffer = new StringBuffer();
                                sbuffer.append(errorResponse.get("status")).append(" ").append(errorResponse.get("scope"));
                                throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), containerResponseContext.getStatus(), sbuffer.toString());
                            }else{
                                throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), containerResponseContext.getStatus(), produceResponse.getOffsets().get(0).getError());
                            }
                        }
                    } else if (ErrorMessage.class.isInstance(containerResponseContext.getEntity())) {
                        generateMetricsForErrorWithoutAuthorization(
                                containerRequestContext, (ErrorMessage) containerResponseContext.getEntity());
                    } else {
                        log.error("Not mapped Class: " + containerResponseContext.getEntityClass().toString());
                    }
                }
                break;
                default: {
                    log.error("Authentication/Authorization failed!");
                    log.error("statusCode=" + containerResponseContext.getStatus());
                    log.error("class=" + containerResponseContext.getEntityClass().getCanonicalName());
                    log.error("responseObject=" + containerResponseContext.getEntity().toString());
                    if (ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
                        ProduceResponse produceResponse = (ProduceResponse) containerResponseContext.getEntity();
                        throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), containerResponseContext.getStatus(), produceResponse.getOffsets().get(0).getError());
                    }
                }
                break;
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

    private void generateAuthenticationException(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext){
        if (ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
            ProduceResponse produceResponse = (ProduceResponse) containerResponseContext.getEntity();
            log.error("Authentication 401 -- Producer Response");
            log.error(produceResponse.toString());
            if(!produceResponse.getOffsets().isEmpty()){
                if (produceResponse.getOffsets().get(0).getError() != null && produceResponse.getOffsets().get(0).getError().contains("required scopes")){
                    IMSRestMetrics.getInstance().incWithoutScope(containerRequestContext, resourceInfo);
                    Map<String, Object> errorResponse = StringsUtil.jsonStringToMap(produceResponse.getOffsets().get(0).getError());
                    if (errorResponse != null) {
                        StringBuffer sbuffer = new StringBuffer();
                        sbuffer.append(errorResponse.get("status")).append(" ").append(errorResponse.get("scope"));
                        throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), sbuffer.toString());
                    }else{
                        throw new IMSRestException(produceResponse.getOffsets().get(0).getErrorCode(), produceResponse.getOffsets().get(0).getError());
                    }
                }
            }
        } else if (ErrorMessage.class.isInstance(containerResponseContext.getEntity())) {
            ErrorMessage error = (ErrorMessage) containerResponseContext.getEntity();
            if ("required scopes".indexOf(error.getMessage()) > -1) {
                IMSRestMetrics.getInstance().incWithoutScope(containerRequestContext, resourceInfo);
            } else if ("Expired Token".indexOf(error.getMessage()) > -1) {
                IMSRestMetrics.getInstance().incExpiredToken(containerRequestContext, resourceInfo);
            }
        } else {
            log.error("Not mapped Class: " + containerResponseContext.getEntityClass().toString());
        }
    }

}
