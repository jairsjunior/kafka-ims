package com.adobe.ids.dim.security.rest.filter;

import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class OAuthCleanerFilter implements ContainerRequestFilter
{
    private static final Logger log = LoggerFactory.getLogger(OAuthCleanerFilter.class);

    public void filter(final ContainerRequestContext requestContext) {
        OAuthCleanerFilter.log.debug("Cleaning up thread " + Thread.currentThread().getName());
        KafkaRestContextProvider.clearCurrentContext();
    }

}
