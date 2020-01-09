package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
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
import java.io.IOException;

public class OAuthResponseFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthResponseFilter.class);

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        log.info("Status Response: " + containerResponseContext.getStatus());
        if(containerResponseContext.hasEntity()) {
            log.info("Entity Response: " + containerResponseContext.getEntity().toString());
        }

        if(containerResponseContext.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            IMSRestMetrics.getInstance().incSucessfull(containerRequestContext, resourceInfo);
        } else {
            switch (containerResponseContext.getStatus()) {
            case 401 : {
                log.debug("Authentication error - Clearing this Principal Context");
                if (!(containerResponseContext.getEntity() instanceof ErrorMessage)) {
                    cleanContextOfPrincipal(containerRequestContext);
                    IMSRestMetrics.getInstance().incWithoutScope(containerRequestContext, resourceInfo);
                }
            }
            break;
            case 403 : {
                log.info("Authorization error! - Add JMX Metrics..");
                if(ProduceResponse.class.isInstance(containerResponseContext.getEntity())) {
                    log.info("403: Producer Response Class");
                    generateMetricsForProducerWithoutAuthorization(containerRequestContext);
                } else if(ErrorMessage.class.isInstance(containerResponseContext.getEntity())) {
                    generateMetricsForErrorWithoutAuthorization(containerRequestContext, (ErrorMessage) containerResponseContext.getEntity());
                } else {
                    log.info("Not mapped Class: " + containerResponseContext.getEntityClass().toString());
                }
            }
            }
        }
    }

    private void cleanContextOfPrincipal(ContainerRequestContext context) throws IMSRestException {
        IMSBearerTokenJwt token = OAuthRestProxyUtil.getBearerInformation(context, resourceInfo);
        KafkaOAuthRestContextFactory.getInstance().cleanSpecificContext(token.principalName());
    }

    private void generateMetricsForProducerWithoutAuthorization(ContainerRequestContext context) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, null);
        IMSRestMetrics.getInstance().incACLDenied(context, resourceInfo, req.getTopics());
    }

    private void generateMetricsForErrorWithoutAuthorization(ContainerRequestContext context, ErrorMessage error) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, error);
        IMSRestMetrics.getInstance().incACLDenied(context, resourceInfo, req.getTopics());
    }

}
