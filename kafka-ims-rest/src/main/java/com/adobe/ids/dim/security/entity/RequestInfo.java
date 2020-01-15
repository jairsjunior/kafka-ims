/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.entity;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.List;

public class RequestInfo {

    private String requestType;
    private String endpoint;
    private List<String> topics;

    public RequestInfo(String requestType, String endpoint, List<String> topics) {
        this.requestType = requestType;
        this.endpoint = endpoint;
        this.topics = topics;
    }

    public String getRequestType() {
        return requestType;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
