/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.rest.context;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import io.confluent.kafkarest.DefaultKafkaRestContext;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.ScalaConsumersContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KafkaOAuthRestContextFactory {

    private static final Logger log = LoggerFactory.getLogger(KafkaOAuthRestContextFactory.class);
    private static final KafkaOAuthRestContextFactory instance = new KafkaOAuthRestContextFactory();
    private final Map<String, KafkaRestContext> userToContextMap;

    private KafkaOAuthRestContextFactory() {
        this.userToContextMap = new HashMap<String, KafkaRestContext>();
    }

    public static KafkaOAuthRestContextFactory getInstance() {
        return KafkaOAuthRestContextFactory.instance;
    }

    public KafkaRestContext getContext(
        final IMSBearerTokenJwt principal,
        final KafkaOAuthSecurityRestConfig kafkaRestConfig,
        final String resourceType,
        final boolean tokenAuth) {
        log.debug("KafkaOAuthRestContextFactory -- getContext");
        String principalWithResourceType = principal.principalName();
        log.debug("Principal With Resource Type: ", principalWithResourceType);
        if (this.userToContextMap.containsKey(principalWithResourceType)) {
            log.debug("has userToContextMap principal: ", principalWithResourceType);
            return this.userToContextMap.get(principalWithResourceType);
        }
        synchronized (principalWithResourceType) {
            log.debug("create userToContextMap principal: ", principalWithResourceType);
            final ScalaConsumersContext scalaConsumersContext =
                new ScalaConsumersContext(kafkaRestConfig);
            final KafkaRestContext context =
                new DefaultKafkaRestContext(kafkaRestConfig, null, null, null, scalaConsumersContext);
            this.userToContextMap.put(principalWithResourceType, context);
        }
        return this.userToContextMap.get(principalWithResourceType);
    }

    public void cleanSpecificContext(String principalName) {
        log.debug("Clean specific context with the principal name: " + principalName);
        try {
            if (this.userToContextMap.containsKey(principalName)) {
                this.userToContextMap.remove(principalName).shutdown();
                log.debug("Cleared context: " + principalName);
            }
        } catch (Exception e) {
            log.warn("This context no longer exists");
        }
    }

    public void clean() {
        log.debug("KafkaOAuthRestContextFactory -- clean");
        for (final KafkaRestContext context : this.userToContextMap.values()) {
            context.shutdown();
        }
        this.userToContextMap.clear();
    }
}
