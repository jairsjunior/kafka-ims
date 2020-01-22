/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.util;

import com.adobe.ids.dim.security.common.exception.IMSRestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class OAuthRestProxyUtilTest {

    @Test(expected = IMSRestException.class)
    public void testTokenNull() {
        OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(null);
    }

    @Test(expected = IMSRestException.class)
    public void testTokenMalformed() {
        OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer("xfghjklkiuyt esdrtfhjlkiy");
    }

    @Test(expected = IMSRestException.class)
    public void testTokenInvalid() {
        OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1Nzg0Mjg4NTYsImV4cCI6MTYwOTk2NDg1NiwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJFbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWluaXN0cmF0b3IiXX0.sM2-kSbb3cPC62s-DjIbDk2mTbwSxvz85oaP6UsPs-4");
    }

    @Test
    public void testTokenValid() {
        assertNotNull(
            OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(
                "eyJ4NXUiOiJpbXNfbmExLXN0ZzEta2V5LTEuY2VyIiwiYWxnIjoiUlMyNTYifQ.eyJpZCI6IjE1NzYxOTYxMjA1NjBfMjIzMWYyODMtMDAxMi00OWFkLTk5MDQtNDQ2ODk2NTc4NjZjX3VlMSIsImNsaWVudF9pZCI6IkRJTV9TRVJWSUNFX0FDQ09VTlQiLCJ1c2VyX2lkIjoiRElNX1NFUlZJQ0VfQUNDT1VOVEBBZG9iZUlEIiwidHlwZSI6ImFjY2Vzc190b2tlbiIsImFzIjoiaW1zLW5hMS1zdGcxIiwicGFjIjoiRElNX1NFUlZJQ0VfQUNDT1VOVF9zdGciLCJydGlkIjoiMTU3NjE5NjEyMDU2MF81YWE1MTcyOS04YzkzLTRlMWUtOWIzNC1mY2YxZGRiNzlkNjRfdWUxIiwicnRlYSI6IjE1Nzc0MDU3MjA1NjAiLCJtb2kiOiIyNGZjNmJlNSIsImMiOiJwWnpjOEFsbVR2cElQS2dwaUNTSGh3PT0iLCJleHBpcmVzX2luIjoiODY0MDAwMDAiLCJzY29wZSI6InN5c3RlbSxvcGVuaWQsQWRvYmVJRCxkaW0uY29yZS5zZXJ2aWNlcyIsImNyZWF0ZWRfYXQiOiIxNTc2MTk2MTIwNTYwIn0.B0uO6LxkVNuQ4kqa8UrxVkCR1hmOLerSrq0zT7ssJiIPZh5siKDSDyMNgAuimdS7gEfooXp-yUMxKORKeZRBbyh-32jrPWpXYTUqbg0RyvWl3CaaWOAwQB5LbEKeBQgMQW_2dOmk-X1gbyogcVZXvs-pgmjEnnHhvc4SJAZuGzeuhf263Ck1fOLlFqIQWm3-o9k4mkoLPTsFeEnh7X8HNYTpCTFgqVnetAHaqvV-pV3bFz0h02ujwouQF2ESpZdU_qNsqwnLqDDxQD-HyHaOeqWUaORh4Wd7LXrZSpdImKXEaNCcjvcBt88GbWanc5Unv7IAROKR5WQr4YNhxxUT5w"));
    }

    @Test
    public void testExtractTopicsFromErrorMessageWithOneTopic() {
        List<String> topics = OAuthRestProxyUtil.extractTopicsFromErrors("Not authorized to access topics: [test-topic]");
        assertNotNull(topics);
        assertEquals(1, topics.size());
        assertEquals("test-topic", topics.get(0));
    }

    @Test
    public void testExtractTopicsFromErrorMessageWithAnyTopics() {
        List<String> topics = OAuthRestProxyUtil.extractTopicsFromErrors("Not authorized to access topics: [test-topic, test-topic-1,test-topic3 ,test-topic-4 , test-topic-5]");
        assertNotNull(topics);
        assertEquals(5, topics.size());
        assertEquals("test-topic", topics.get(0));
        assertEquals("test-topic-1", topics.get(1));
        assertEquals("test-topic-5", topics.get(4));
    }
}
