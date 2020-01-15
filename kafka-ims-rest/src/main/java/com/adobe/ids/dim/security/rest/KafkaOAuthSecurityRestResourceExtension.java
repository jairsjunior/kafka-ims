/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.rest;

import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.rest.filter.OAuthCleanerFilter;
import com.adobe.ids.dim.security.rest.filter.OAuthRequestFilter;
import com.adobe.ids.dim.security.rest.filter.OAuthResponseFilter;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.extension.RestResourceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configurable;

public class KafkaOAuthSecurityRestResourceExtension implements RestResourceExtension {

    private static final Logger log =
        LoggerFactory.getLogger(KafkaOAuthSecurityRestResourceExtension.class);

    public void register(Configurable<?> config, KafkaRestConfig restConfig) {
        try {
            log.debug("KafkaOAuthSecurityRestResourceExtension -- register");
            final KafkaOAuthSecurityRestConfig secureKafkaRestConfig =
                new KafkaOAuthSecurityRestConfig(restConfig.getOriginalProperties(), null);
            log.debug("KafkaOAuthSecurityRestResourceExtension -- registering OAuthfilter");
            // Change the name of class to OAuthRequestFilter..
            config.register(new OAuthRequestFilter(secureKafkaRestConfig));
            config.register(OAuthCleanerFilter.class);
            config.register(new OAuthResponseFilter());

        } catch (Exception e) {
            log.error("KafkaOAuthSecurityRestResourceExtension -- exception: ", e);
        }
    }

    public void clean() {
        KafkaOAuthRestContextFactory.getInstance().clean();
    }
}
