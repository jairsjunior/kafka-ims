package com.adobe.ids.dim.security.metrics;

public interface OAuthMetricsMBean {
    Integer getSuccessfullRequestCount();
    Integer getInvalidTokenErrorCount();
    Integer getExpiredTokenErrorCount();
}
