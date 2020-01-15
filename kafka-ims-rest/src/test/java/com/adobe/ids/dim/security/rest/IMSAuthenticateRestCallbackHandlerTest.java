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
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthRestProxyUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class IMSAuthenticateRestCallbackHandlerTest {

    IMSAuthenticateRestCallbackHandler handler;
    Callback[] callbacks;
    IMSBearerTokenJwt imsBearerValidTokenJwt;

    @Before
    public void setUp() {
        handler = new IMSAuthenticateRestCallbackHandler();
        handler.setConfigured(true);
        PowerMockito.mockStatic(OAuthRestProxyUtil.class);
        callbacks = new Callback[1];
        OAuthBearerTokenCallback oAuthBearerTokenCallback = new OAuthBearerTokenCallback();
        callbacks[0] = oAuthBearerTokenCallback;

        Map<String, Object> jwtToken = new HashMap<String, Object>();
        jwtToken.put("client_id", "DIM_SERVICE_ACCOUNT");
        jwtToken.put("scope", "dim.core.services");

        // 24 hr expiry time
        Calendar nextDay = Calendar.getInstance();
        nextDay.setTime(new Date());
        nextDay.add(Calendar.DATE, +1);
        jwtToken.put("expires_in", String.valueOf(nextDay.getTimeInMillis()));
        jwtToken.put("created_at", String.valueOf(System.currentTimeMillis()));

        String randomAccessToken = "asdjfklsdfoeuirrnmncxoiuereklr";
        imsBearerValidTokenJwt = new IMSBearerTokenJwt(jwtToken, randomAccessToken);
    }

    @Test
    public void testValidToken() throws IOException, UnsupportedCallbackException {
        when(OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer("token.test"))
        .thenReturn(imsBearerValidTokenJwt);
        Map<String, String> moduleOptions = new HashMap<>();
        moduleOptions.put("ims.access.token", "token.test");
        handler.setModuleOptions(moduleOptions);
        handler.handle(callbacks);
        assertNotNull(((OAuthBearerTokenCallback) callbacks[0]).token());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTokenNull() throws IOException, UnsupportedCallbackException {
        Map<String, String> moduleOptions = new HashMap<>();
        moduleOptions.put("ims.access.token", null);
        handler.setModuleOptions(moduleOptions);
        handler.handle(callbacks);
    }

    @Test
    public void testConfigureForValidParameters() {
        IMSAuthenticateRestCallbackHandler iMSAuthenticateRestCallbackHandler =
            new IMSAuthenticateRestCallbackHandler();
        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.access.token", "sdfsdfsdf");

        AppConfigurationEntry firstItem =
            new AppConfigurationEntry(
            "OAuthBearerLoginModule", LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        iMSAuthenticateRestCallbackHandler.configure(null, "OAUTHBEARER", jaasConfigEntries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidParametersPassed_thenIllegalArgumentExceptionIsThrown() {
        IMSAuthenticateRestCallbackHandler iMSAuthenticateRestCallbackHandler =
            new IMSAuthenticateRestCallbackHandler();
        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.access.token", "sdfsdfsdfer");

        AppConfigurationEntry firstItem =
            new AppConfigurationEntry(
            "OAuthBearerLoginModule", LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        iMSAuthenticateRestCallbackHandler.configure(null, "OAUTHBEA", jaasConfigEntries);
    }
}
