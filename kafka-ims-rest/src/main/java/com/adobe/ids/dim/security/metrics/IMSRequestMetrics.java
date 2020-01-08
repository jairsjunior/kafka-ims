package com.adobe.ids.dim.security.metrics;

public class IMSRequestMetrics implements IMSRequestMetricsMBean {

    private Integer countOfRequestSuccess;
    private Integer countOfRequestFailedInvalidToken;
    private Integer countOfRequestFailedExpiredToken;
    private Integer countOfRequestFailedWithoutScope;

    public IMSRequestMetrics(){
        this.countOfRequestSuccess = 0;
        this.countOfRequestFailedInvalidToken = 0;
        this.countOfRequestFailedExpiredToken = 0;
        this.countOfRequestFailedWithoutScope = 0;
    }

    @Override
    public Integer getSuccessfullRequestCount() {
        return countOfRequestSuccess;
    }

    @Override
    public Integer getInvalidTokenErrorCount() {
        return countOfRequestFailedInvalidToken;
    }

    @Override
    public Integer getExpiredTokenErrorCount() {
        return countOfRequestFailedExpiredToken;
    }

    @Override
    public Integer getWithoutScopeErrorCount() {
        return countOfRequestFailedWithoutScope;
    }

    public void incCountOfRequestSuccess(){
        this.countOfRequestSuccess++;
    }

    public void incCountOfRequestFailedInvalidToken(){
        this.countOfRequestFailedInvalidToken++;
    }

    public void incCountOfRequestsFailedExpiredToken(){ this.countOfRequestFailedExpiredToken++; }

    public void incCountOfRequestsFailedWithoutScope() { this.countOfRequestFailedWithoutScope++; }


}
