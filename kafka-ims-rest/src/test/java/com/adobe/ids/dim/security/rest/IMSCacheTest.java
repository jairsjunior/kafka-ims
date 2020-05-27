package com.adobe.ids.dim.security.rest;

import com.adobe.ids.dim.security.common.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.rest.cache.CachePrincipal;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import io.confluent.rest.RestConfigException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IMSCacheTest {

    CachePrincipal principal;
    KafkaOAuthSecurityRestConfig item;
    IMSBearerTokenJwt imsBearerTokenJwt;

    @Before
    public void setUp() throws RestConfigException {
        principal = new CachePrincipal(5, TimeUnit.SECONDS);

        Properties prop = new Properties();
        prop.put("test", "test.value");

        Map<String, Object> jwtToken = new HashMap<String, Object>();
        jwtToken.put("client_id", "DIM_SERVICE_ACCOUNT");
        jwtToken.put("scope", "dim.core.services");
        jwtToken.put("expires_in", "600");
        jwtToken.put("created_at", "0");
        String randomAccessToken = "asdjfklsdfoeuirrnmncxoiuereklr";

        imsBearerTokenJwt = new IMSBearerTokenJwt(jwtToken, randomAccessToken);
        item = new KafkaOAuthSecurityRestConfig(prop, imsBearerTokenJwt);
    }

    @Test
    public void TestCacheInsert(){
        principal.put("test.principal", item);
        assertEquals(item, principal.get("test.principal"));
    }

    @Test
    public void TestCacheInvalidate(){
        principal.put("test.principal", item);
        assertEquals(item, principal.get("test.principal"));
        principal.remove("test.principal");
        assertNull(principal.get("test.principal"));
    }

    @Test
    public void TestCacheInvalidateByTime() throws InterruptedException {
        principal.put("test.principal", item);
        assertEquals(item, principal.get("test.principal"));
        Thread.sleep(6000);
        assertNull(principal.get("test.principal"));
    }

}
