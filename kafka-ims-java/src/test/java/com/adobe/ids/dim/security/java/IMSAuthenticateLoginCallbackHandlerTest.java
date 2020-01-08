package com.adobe.ids.dim.security.java;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.IMSHttpCalls;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IMSHttpCalls.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class IMSAuthenticateLoginCallbackHandlerTest {

    IMSAuthenticateLoginCallbackHandler handler;
    Callback[] callbacks;
    IMSBearerTokenJwt imsBearerTokenJwt;

    @Before
    public void setUp() {
        handler = new IMSAuthenticateLoginCallbackHandler();
        handler.setConfigured(true);
        PowerMockito.mockStatic(IMSHttpCalls.class);
        callbacks = new Callback[1];
        OAuthBearerTokenCallback oAuthBearerTokenCallback = new OAuthBearerTokenCallback();
        callbacks[0] = oAuthBearerTokenCallback;

        Map<String, Object> jwtToken = new HashMap<String, Object>();
        jwtToken.put("client_id", "DIM_SERVICE_ACCOUNT");
        jwtToken.put("scope", "dim.core.services");
        jwtToken.put("expires_in", "86400000");
        jwtToken.put("created_at", "1569630920420");

        String randomAccessToken = "asdjfklsdfoeuirrnmncxoiuereklr";
        imsBearerTokenJwt = new IMSBearerTokenJwt(jwtToken, randomAccessToken);
    }

    @Test
    public void testHandleValidToken() throws IOException, UnsupportedCallbackException {
        when(IMSHttpCalls.getIMSToken(anyMap())).thenReturn(imsBearerTokenJwt);
        handler.handle(callbacks);
        assertNotNull(((OAuthBearerTokenCallback) callbacks[0]).token());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNullTokenPassed_thenIllegalArgumentExceptionIsThrown()
            throws IOException, UnsupportedCallbackException {
        when(IMSHttpCalls.getIMSToken(anyMap())).thenReturn(null);
        handler.handle(callbacks);
    }

    @Test
    public void testConfigureForValidParameters() {
        IMSAuthenticateLoginCallbackHandler imsAuthenticateLoginCallbackHandlerTest = new IMSAuthenticateLoginCallbackHandler();

        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.token.url", "test_url");
        options.put("ims.client.id", "test_client_id");
        options.put("ims.client.code", "test_client_code");
        options.put("ims.client.secret", "test_client_secret");
        options.put("ims.grant.type", "test_grant_type");

        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
                LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        imsAuthenticateLoginCallbackHandlerTest.configure(null, "OAUTHBEARER", jaasConfigEntries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidParametersPassed_thenIllegalArgumentExceptionIsThrown() {
        IMSAuthenticateLoginCallbackHandler imsAuthenticateLoginCallbackHandlerTest = new IMSAuthenticateLoginCallbackHandler();

        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.token.url", "test_url");
        options.put("ims.client.id", "test_client_id");
        options.put("ims.client.", "test_client_code");
        options.put("ims.client.", "test_client_secret");
        options.put("ims.grant.", "test_grant_type");

        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
        LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        imsAuthenticateLoginCallbackHandlerTest.configure(null, "OAUTHBEA", jaasConfigEntries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMissingParametersPassed_thenIllegalArgumentExceptionIsThrown() {
        IMSAuthenticateLoginCallbackHandler imsAuthenticateLoginCallbackHandlerTest = new IMSAuthenticateLoginCallbackHandler();

        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.token.url", "test_url");
        options.put("ims.client.id", "test_client_id");

        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
        LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        imsAuthenticateLoginCallbackHandlerTest.configure(null, "OAUTHBEARER", jaasConfigEntries);
    }

}