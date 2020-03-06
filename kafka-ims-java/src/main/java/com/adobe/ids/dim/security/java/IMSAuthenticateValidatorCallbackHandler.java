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
import com.adobe.ids.dim.security.common.exception.IMSValidatorException;
import com.adobe.ids.dim.security.metrics.OAuthMetricsValidator;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerExtensionsValidatorCallback;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerValidatorCallback;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerIllegalTokenException;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerValidationResult;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IMSAuthenticateValidatorCallbackHandler implements AuthenticateCallbackHandler {
    private static final String IMS_ENDPOINT_TOKEN_VALIDATION_CONFIG = "ims.token.validation.url";
    private static final String DIM_CORE_SCOPE = "dim.core.services";
    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateValidatorCallbackHandler.class);
    private Map<String, ?> moduleOptions = null;
    private boolean configured = false;

    // Used for unit testing the method call on configure()
    private boolean registerMetrics = true;
    // Allowed scopes
    private Time time = Time.SYSTEM;

    @Override
    public void configure(Map<String, ?> map, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(saslMechanism))
            throw new IllegalArgumentException(
                String.format("Unexpected SASL mechanism: %s", saslMechanism));

        this.moduleOptions = Collections.unmodifiableMap(jaasConfigEntries.get(0).getOptions());

        if (!moduleOptions.containsKey(IMS_ENDPOINT_TOKEN_VALIDATION_CONFIG)
                || StringsUtil.isNullOrEmpty(
                    (String) moduleOptions.get(IMS_ENDPOINT_TOKEN_VALIDATION_CONFIG))) {
            throw new IllegalArgumentException(
                "Missing " + IMS_ENDPOINT_TOKEN_VALIDATION_CONFIG + " in jaas config.");
        }

        configured = true;
        if (registerMetrics) {
            registerMetrics();
        }
    }

    public void setIsRegisterMetrics(boolean registerMetrics) {
        this.registerMetrics = registerMetrics;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    @Override
    public void close() {
    }

    public void registerMetrics() {
        try {
            StringBuilder str = new StringBuilder("kafka.broker");
            str.append(":name=").append("ims-metrics");
            ManagementFactory.getPlatformMBeanServer().registerMBean(OAuthMetricsValidator.getInstance(), new ObjectName(str.toString()));
        } catch (Exception e) {
            log.error("Error on register MBean Server for JMX metrics");
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (!isConfigured())
            throw new IllegalStateException("Callback handler not configured");
        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerValidatorCallback) {
                OAuthBearerValidatorCallback validationCallback = (OAuthBearerValidatorCallback) callback;
                try {
                    handleCallback(validationCallback);
                } catch (OAuthBearerIllegalTokenException e) {
                    OAuthBearerValidationResult failureReason = e.reason();
                    validationCallback.error(failureReason.failureDescription(), failureReason.failureScope(),
                            failureReason.failureOpenIdConfig());
                }
            } else if (callback instanceof OAuthBearerExtensionsValidatorCallback) {
                OAuthBearerExtensionsValidatorCallback extensionsCallback = (OAuthBearerExtensionsValidatorCallback) callback;
                extensionsCallback.inputExtensions().map()
                        .forEach((extensionName, v) -> extensionsCallback.valid(extensionName));
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleCallback(OAuthBearerValidatorCallback callback)
    throws IllegalArgumentException {
        String accessToken = callback.tokenValue();
        if (accessToken == null)
            throw new IllegalArgumentException("Callback missing required token value");

        IMSBearerTokenJwt token = IMSHttpCalls.validateIMSToken(accessToken, moduleOptions);

        // Check if Token has expired
        long now = time.milliseconds();

        log.debug("Token expiration time: {}", token.lifetimeMs());

        if (now > token.lifetimeMs()) {
            log.debug("Token has expired! Needs refresh");
            OAuthBearerValidationResult.newFailure("Expired Token").throwExceptionIfFailed();
        }

        // Check if we have DIM specific scope in the token or not
        Set<String> scopes = token.scope();

        if (!scopes.contains(DIM_CORE_SCOPE)) {
            OAuthMetricsValidator.getInstance().incCountOfRequestsFailedWithoutScope();
            log.debug("Token doesn't have required scopes! We cannot accept this token");
            log.debug("Required scope is: {}", DIM_CORE_SCOPE);
            log.debug("Token has following scopes: {}", scopes);
            OAuthBearerValidationResult.newFailure(
                IMSValidatorException.KAFKA_EXCEPTION_WITHOUT_SCOPE_MSG,
                "Missing scope " + DIM_CORE_SCOPE,
                "")
            .throwExceptionIfFailed();
        }

        log.debug("Validated IMS Token: {}", token.toString());
        callback.token(token);
    }
}
