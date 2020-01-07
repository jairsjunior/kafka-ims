package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.metrics.OAuthMetrics;
import com.adobe.ids.dim.security.metrics.TestConfluentMetricsClass;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
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
        log.debug("Status Response: " + containerResponseContext.getStatus());

        log.debug("Entity Response: " + containerResponseContext.getEntity().toString());

        if(containerResponseContext.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            OAuthMetrics.getInstance().incCountOfRequestSuccess();
        } else {
            switch (containerResponseContext.getStatus()) {
                case 401 : {
                    log.debug("Authentication error! Clearing this Principal Context");
                    if (!(containerResponseContext.getEntity() instanceof ErrorMessage)){
                        cleanContextOfPrincipal(containerRequestContext);
                        //Testing Dynamically JMX Metrics Creation
                        if(TestConfluentMetricsClass.getInstance().getSensor("test.response.sensor") == null ){
                            TestConfluentMetricsClass.getInstance().addSensorCount("test.response.sensor", "ims-dynamically-test", "Description Of Metric", "topic", OAuthRestProxyUtil.mountResquestInfo(containerRequestContext, resourceInfo).getTopic());
                        }
                        TestConfluentMetricsClass.getInstance().getSensor("test.response.sensor").record();
                    }
                } break;
                case 403 : {
                    log.debug("Authorization error! - Add JMX Metrics..");
                }
            }
        }
    }

    private void cleanContextOfPrincipal(ContainerRequestContext context) throws IMSRestException {
        IMSBearerTokenJwt token = OAuthRestProxyUtil.getBearerInformation(context);
        KafkaOAuthRestContextFactory.getInstance().cleanSpecificContext(token.principalName());
    }
}
