package com.adobe.ids.dim.security.metrics;

public class IMSACLMetrics implements IMSACLMetricsMBean {

    private Integer countWithoutACLRequestCount;

    public IMSACLMetrics(){
        this.countWithoutACLRequestCount=0;
    }

    @Override
    public Integer getACLDeniedRequestCount() {
        return countWithoutACLRequestCount;
    }

    public void incACLDeniedACLRequestCount(){
        this.countWithoutACLRequestCount++;
    }
}
