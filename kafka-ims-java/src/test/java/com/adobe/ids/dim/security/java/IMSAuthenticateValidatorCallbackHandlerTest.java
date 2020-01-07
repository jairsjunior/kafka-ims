package com.adobe.ids.dim.security.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.common.IMSHttpCalls;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerValidatorCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IMSHttpCalls.class })
public class IMSAuthenticateValidatorCallbackHandlerTest {

    IMSAuthenticateValidatorCallbackHandler handler;
    Callback[] callbacks;
    IMSBearerTokenJwt imsBearerValidTokenJwt, expiredJwtToken, invalidScopeJwt;

    @Before
    public void setUp() {
        handler = new IMSAuthenticateValidatorCallbackHandler();
        handler.setConfigured(true);
        PowerMockito.mockStatic(IMSHttpCalls.class);
        callbacks = new Callback[1];
        OAuthBearerValidatorCallback oAuthBearerValidatorCallback = new OAuthBearerValidatorCallback("token");
        callbacks[0] = oAuthBearerValidatorCallback;

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

        Map<String, Object> jwtTokenExpired = new HashMap<String, Object>();
        jwtTokenExpired.put("client_id", "DIM_SERVICE_ACCOUNT");
        jwtTokenExpired.put("scope", "dim.core.services");
        jwtTokenExpired.put("expires_in", "0");
        jwtTokenExpired.put("created_at", String.valueOf(System.currentTimeMillis()));

        expiredJwtToken = new IMSBearerTokenJwt(jwtTokenExpired, randomAccessToken);

        // 24 hr expiry time
        jwtToken.put("expires_in", String.valueOf(nextDay.getTimeInMillis()));
        jwtToken.put("created_at", String.valueOf(System.currentTimeMillis()));
        jwtToken.put("scope", "dim.core.ser");

        invalidScopeJwt = new IMSBearerTokenJwt(jwtToken, randomAccessToken);
    }

    @Test
    public void testValidToken() throws IOException, UnsupportedCallbackException {
        when(IMSHttpCalls.validateIMSToken("token", null)).thenReturn(imsBearerValidTokenJwt);
        handler.handle(callbacks);
        assertNotNull(((OAuthBearerValidatorCallback) callbacks[0]).token());
    }

    @Test
    public void testTokenExpiry() throws IOException, UnsupportedCallbackException {
        when(IMSHttpCalls.validateIMSToken("token", null)).thenReturn(expiredJwtToken);
        handler.handle(callbacks);
        assertEquals(((OAuthBearerValidatorCallback) callbacks[0]).errorStatus(), "Expired Token");
    }

    @Test
    public void testInvalidScope() throws IOException, UnsupportedCallbackException {
        when(IMSHttpCalls.validateIMSToken("token", null)).thenReturn(invalidScopeJwt);
        handler.handle(callbacks);
        assertEquals(((OAuthBearerValidatorCallback) callbacks[0]).errorStatus(),
                "Token doesn't have required scopes! We cannot accept this token. Please work with DIM team to get needed scopes added");
    }

    /* @Test
    public void testConfigureForValidParameters() {
        IMSAuthenticateValidatorCallbackHandler iMSAuthenticateValidatorCallbackHandlerTest = new IMSAuthenticateValidatorCallbackHandler();
        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.token.validation.url", "test_url");
    
        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
                LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        iMSAuthenticateValidatorCallbackHandlerTest.configure(null, "OAUTHBEARER", jaasConfigEntries);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidParametersPassed_thenIllegalArgumentExceptionIsThrown() {
        IMSAuthenticateValidatorCallbackHandler iMSAuthenticateValidatorCallbackHandlerTest = new IMSAuthenticateValidatorCallbackHandler();
        Map<String, String> options = new HashMap<String, String>();
        options.put("ims.token.validation.url", "test_url");
    
        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
                LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
        iMSAuthenticateValidatorCallbackHandlerTest.configure(null, "OAUTHBEA", jaasConfigEntries);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void whenMissingParametersPassed_thenIllegalArgumentExceptionIsThrown() {
        IMSAuthenticateValidatorCallbackHandler iMSAuthenticateValidatorCallbackHandlerTest = new IMSAuthenticateValidatorCallbackHandler();
        Map<String, String> options = new HashMap<String, String>();
    
        AppConfigurationEntry firstItem = new AppConfigurationEntry("OAuthBearerLoginModule",
                LoginModuleControlFlag.REQUIRED, options);
        List<AppConfigurationEntry> jaasConfigEntries = new ArrayList<AppConfigurationEntry>();
        jaasConfigEntries.add(firstItem);
    
        iMSAuthenticateValidatorCallbackHandlerTest.configure(null, "OAUTHBEARER", jaasConfigEntries);
    } */

}