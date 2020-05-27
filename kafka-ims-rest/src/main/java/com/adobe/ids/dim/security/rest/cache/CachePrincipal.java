package com.adobe.ids.dim.security.rest.cache;

import avro.shaded.com.google.common.cache.CacheBuilder;
import avro.shaded.com.google.common.cache.CacheLoader;
import avro.shaded.com.google.common.cache.LoadingCache;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachePrincipal {

    private static final Logger log = LoggerFactory.getLogger(CachePrincipal.class);
    private LoadingCache<String, KafkaOAuthSecurityRestConfig> mapConfiguration;
    private static CachePrincipal principal;

    public static CachePrincipal getInstance(){
        if(principal == null){
            principal = new CachePrincipal(5, TimeUnit.MINUTES);
        }
        return principal;
    }

    public CachePrincipal(long duration, TimeUnit timeUnit){
        this.mapConfiguration = CacheBuilder.newBuilder()
                .expireAfterWrite(duration, timeUnit)
                .concurrencyLevel(10)
                .build(new CacheLoader<String, KafkaOAuthSecurityRestConfig>() {
                    @Override
                    public KafkaOAuthSecurityRestConfig load(String s) {
                        return null;
                    }
                });
    }

    public KafkaOAuthSecurityRestConfig get(String s) {
        KafkaOAuthSecurityRestConfig result = null;
        try {
            String hashKey = UUID.nameUUIDFromBytes(s.getBytes()).toString();
            result = this.mapConfiguration.get(hashKey);
            log.debug("Get configuration from cache map");
            log.debug("Key: " + s);
            log.debug("HashKey: " + hashKey);
            log.debug("Map: " + result.toString());
        } catch (ExecutionException e) {
            log.debug("Concurrent access to cache configuration of dinamically jaas file. Creating a new one.");
        } catch (CacheLoader.InvalidCacheLoadException e) {
            log.trace("Principal not found on cache. Return null to creating a new one");
        }
        return result;
    }

    public void put(String s, KafkaOAuthSecurityRestConfig item){
        String hashKey = UUID.nameUUIDFromBytes(s.getBytes()).toString();
        log.debug("Put configuration on cache map");
        log.debug("Key: " + s);
        log.debug("HashKey: " + hashKey);
        log.debug("Map: " + item.toString());
        this.mapConfiguration.put(hashKey, item);
    }

    public void remove(String s){
        String hashKey = UUID.nameUUIDFromBytes(s.getBytes()).toString();
        log.debug("Remove configuration from cache map");
        log.debug("Key: " + s);
        log.debug("HashKey: " + hashKey);
        this.mapConfiguration.invalidate(hashKey);
    }

}
