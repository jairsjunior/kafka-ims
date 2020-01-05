package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.metrics.OAuthMetrics;
import com.adobe.ids.dim.security.metrics.TestConfluentMetricsClass;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
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
        log.info("Entity Response: " + containerResponseContext.getEntity().toString());

        if(containerResponseContext.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)){
            OAuthMetrics.getInstance().getSuccessfullRequestCount();
        }else{
            switch (containerResponseContext.getStatus()) {
                case 401 : {
                    log.info("Authentication error! Clearing this Principal Context");
                    cleanContextOfPrincipal(containerRequestContext);

                    //Testing Dynamically JMX Metrics Creation
                    TestConfluentMetricsClass.getInstance().addSensorCount("test.response.sensor", "ims-dinamically-test", "Description Of Metric", "topic", OAuthRestProxyUtil.mountResquestInfo(containerRequestContext, resourceInfo).getTopic());
                    TestConfluentMetricsClass.getInstance().getSensor("test.response.sensor").record();
                } break;
                case 403 : {
                    log.info("Authorization error! - Add JMX Metrics..");
                }
            }
        }
    }

    private void cleanContextOfPrincipal(ContainerRequestContext context){
        IMSBearerTokenJwt token = OAuthRestProxyUtil.getBearerInformation(context);
        KafkaOAuthRestContextFactory.getInstance().cleanSpecificContext(token.principalName());
    }
}
