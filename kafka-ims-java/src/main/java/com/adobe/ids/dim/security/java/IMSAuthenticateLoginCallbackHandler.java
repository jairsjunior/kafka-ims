/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.java;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.IMSHttpCalls;
import com.adobe.ids.dim.security.common.StringsUtil;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IMSAuthenticateLoginCallbackHandler implements AuthenticateCallbackHandler {

    public static final String IMS_CLIENT_ID_CONFIG = "ims.client.id";
    public static final String IMS_CLIENT_CODE_CONFIG = "ims.client.code";
    public static final String IMS_CLIENT_SECRET_CONFIG = "ims.client.secret";
    public static final String IMS_ENDPOINT_CONFIG = "ims.token.url";
    public static final String IMS_GRANT_TYPE_CONFIG = "ims.grant.type";

    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateLoginCallbackHandler.class);
    private Map<String, ?> moduleOptions = null;
    private boolean configured = false;

    @Override
    public void configure(
        Map<String, ?> map, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        log.debug("IMSAuthenticateLoginCallbackHandler configure");
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(saslMechanism)) {
            log.debug(String.format("Unexpected SASL mechanism: %s", saslMechanism));
            throw new IllegalArgumentException(
                String.format("Unexpected SASL mechanism: %s", saslMechanism));
        }

        this.moduleOptions = Collections.unmodifiableMap(jaasConfigEntries.get(0).getOptions());
        ;

        if (!moduleOptions.containsKey(IMS_ENDPOINT_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_ENDPOINT_CONFIG))) {
            throw new IllegalArgumentException("Missing " + IMS_ENDPOINT_CONFIG + " in jaas config.");
        }

        if (!moduleOptions.containsKey(IMS_CLIENT_ID_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_CLIENT_ID_CONFIG))) {
            throw new IllegalArgumentException("Missing " + IMS_CLIENT_ID_CONFIG + " in jaas config.");
        }

        if (!moduleOptions.containsKey(IMS_CLIENT_CODE_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_CLIENT_CODE_CONFIG))) {
            throw new IllegalArgumentException("Missing " + IMS_CLIENT_CODE_CONFIG + " in jaas config.");
        }

        if (!moduleOptions.containsKey(IMS_CLIENT_SECRET_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_CLIENT_SECRET_CONFIG))) {
            throw new IllegalArgumentException(
                "Missing " + IMS_CLIENT_SECRET_CONFIG + " in jaas config.");
        }

        if (!moduleOptions.containsKey(IMS_GRANT_TYPE_CONFIG)
                || StringsUtil.isNullOrEmpty((String) moduleOptions.get(IMS_GRANT_TYPE_CONFIG))) {
            throw new IllegalArgumentException("Missing " + IMS_GRANT_TYPE_CONFIG + " in jaas config.");
        }

        configured = true;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    @Override
    public void close() {}

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        log.debug("IMSAuthenticateLoginCallbackHandler handle");
        if (!isConfigured()) {
            log.debug("Callback handler not configured");
            throw new IllegalStateException("Callback handler not configured");
        }

        log.debug("For each callback received on handle");
        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                try {
                    log.debug("callback is an instance of OAuthBearerTokenCallback");
                    handleCallback((OAuthBearerTokenCallback) callback);
                } catch (KafkaException e) {
                    log.debug("on handleCallback");
                    throw new IOException(e.getMessage(), e);
                }
            } else {
                log.debug("Callback is not a instance of OAuthBearerTokenCallback", callback.getClass().getName());
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleCallback(OAuthBearerTokenCallback callback) {
        log.debug("IMSAuthenticateLoginCallbackHandler handleCallback");
        if (callback.token() != null) {
            log.debug("Callback had a token already");
            throw new IllegalArgumentException("Callback had a token already");
        }

        IMSBearerTokenJwt token = null;

        try{
            token = IMSHttpCalls.getIMSToken(moduleOptions);
        }catch (Exception e){
            log.debug("Error during get IMS Token:", e);
            throw new IllegalArgumentException(e.getMessage());
        }

        if (token == null) {
            log.debug("Null token returned from server");
            throw new IllegalArgumentException("Null token returned from server");
        }

        log.debug("Retrieved IMS Token: {}", token.toString());
        callback.token(token);
    }
}
