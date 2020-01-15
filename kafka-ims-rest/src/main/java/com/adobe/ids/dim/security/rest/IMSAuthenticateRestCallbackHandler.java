/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.rest;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.StringsUtil;
import com.adobe.ids.dim.security.common.exception.IMSRestException;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
import kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IMSAuthenticateRestCallbackHandler implements AuthenticateCallbackHandler {

    public static final String IMS_ACCESS_TOKEN_CONFIG = "ims.access.token";

    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateRestCallbackHandler.class);
    private Map<String, ?> moduleOptions = null;
    private boolean configured = false;

    @Override
    public void configure(
        Map<String, ?> map, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(saslMechanism))
            throw new IllegalArgumentException(
                String.format("Unexpected SASL mechanism: %s", saslMechanism));
        if (jaasConfigEntries.get(0) == null)
            throw new IllegalArgumentException(
                String.format("Must supply at least 1 non-null JAAS mechanism configuration (size was %d)",
                              jaasConfigEntries.size()));
        this.moduleOptions = Collections.unmodifiableMap(jaasConfigEntries.get(0).getOptions());

        if (!moduleOptions.containsKey(IMS_ACCESS_TOKEN_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_ACCESS_TOKEN_CONFIG))) {
            throw new IllegalArgumentException("Missing " + IMS_ACCESS_TOKEN_CONFIG + " in jaas config.");
        }

        configured = true;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public void setModuleOptions(Map<String, ?> moduleOptions) {
        this.moduleOptions = moduleOptions;
    }

    @Override
    public void close() {}

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (!isConfigured()) throw new IllegalStateException("Callback handler not configured");

        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                try {
                    handleCallback((OAuthBearerTokenCallback) callback);
                } catch (KafkaException e) {
                    throw new IOException(e.getMessage(), e);
                }
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleCallback(OAuthBearerTokenCallback callback) {
        if (callback.token() != null)
            throw new IllegalArgumentException("Callback had a token already");

        String tokenCode = (String) moduleOptions.get("ims.access.token");
        if (tokenCode == null) {
            throw new IllegalArgumentException("Null token passed in JAAS config file");
        }
        IMSBearerTokenJwt token;
        try {
            token = OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(tokenCode);
        } catch (IMSRestException e) {
            log.error("Null token passed in JAAS config file");
            throw new IllegalArgumentException("Null token passed in JAAS config file");
        }

        log.debug("Jaas file IMS Token: {}", tokenCode);
        callback.token(token);
    }
}
