package com.adobe.ids.dim.security.metrics;

public interface IMSRequestMetricsMBean {
    Integer getSuccessfullRequestCount();
    Integer getInvalidTokenErrorCount();
    Integer getExpiredTokenErrorCount();
    Integer getWithoutScopeErrorCount();

}
