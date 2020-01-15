/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.metrics;

public class IMSACLMetrics implements IMSACLMetricsMBean {

    private Integer countWithoutACLRequestCount;

    public IMSACLMetrics() {
        this.countWithoutACLRequestCount = 0;
    }

    @Override
    public Integer getACLDeniedRequestCount() {
        return countWithoutACLRequestCount;
    }

    public void incACLDeniedACLRequestCount() {
        this.countWithoutACLRequestCount++;
    }
}
