package com.adobe.ids.dim.security.metrics;

public class OAuthMetricsValidator implements OAuthMetricsValidatorMBean {

    private static OAuthMetricsValidator oAuthMetrics;
    private Integer countOfRequestFailedWithoutScope;

    public OAuthMetricsValidator(){
        this.countOfRequestFailedWithoutScope = 0;
    }

    @Override
    public Integer getCountOfRequestFailedWithoutScope() {
        return countOfRequestFailedWithoutScope;
    }

    public void incCountOfRequestsFailedWithoutScope(){
        this.countOfRequestFailedWithoutScope++;
    }

    public static OAuthMetricsValidator getInstance(){
        if(oAuthMetrics == null){
            oAuthMetrics = new OAuthMetricsValidator();
        }
        return oAuthMetrics;
    }

}
