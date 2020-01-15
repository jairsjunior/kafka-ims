/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.metrics;

public class IMSRequestMetrics implements IMSRequestMetricsMBean {

    private Integer countOfRequestSuccess;
    private Integer countOfRequestFailedInvalidToken;
    private Integer countOfRequestFailedExpiredToken;
    private Integer countOfRequestFailedWithoutScope;

    public IMSRequestMetrics() {
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

    public void incCountOfRequestSuccess() {
        this.countOfRequestSuccess++;
    }

    public void incCountOfRequestFailedInvalidToken() {
        this.countOfRequestFailedInvalidToken++;
    }

    public void incCountOfRequestsFailedExpiredToken() {
        this.countOfRequestFailedExpiredToken++;
    }

    public void incCountOfRequestsFailedWithoutScope() {
        this.countOfRequestFailedWithoutScope++;
    }


}
