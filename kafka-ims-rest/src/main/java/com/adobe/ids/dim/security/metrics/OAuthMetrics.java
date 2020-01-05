package com.adobe.ids.dim.security.metrics;

public class OAuthMetrics implements OAuthMetricsMBean {

    private static OAuthMetrics oAuthMetrics;
    private Integer countOfRequestSuccess;
    private Integer countOfRequestFailedInvalidToken;
    private Integer countOfRequestFailedExpiredToken;

    public OAuthMetrics(){
        this.countOfRequestSuccess = 0;
        this.countOfRequestFailedInvalidToken = 0;
        this.countOfRequestFailedExpiredToken = 0;
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

    public void incCountOfRequestSuccess(){
        this.countOfRequestSuccess++;
    }

    public void incCountOfRequestFailedInvalidToken(){
        this.countOfRequestFailedInvalidToken++;
    }

    public void incCountOfRequestsFailedExpiredToken(){
        this.countOfRequestFailedExpiredToken++;
    }

    public static OAuthMetrics getInstance(){
        if(oAuthMetrics == null){
            oAuthMetrics = new OAuthMetrics();
        }
        return oAuthMetrics;
    }

}
