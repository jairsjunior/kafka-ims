package com.adobe.ids.dim.security.entity;

import javax.ws.rs.container.ContainerRequestContext;

public class RequestInfo {

    private String requestType;
    private String topic;

    public RequestInfo(String requestType, String topic) {
        this.requestType = requestType;
        this.topic = topic;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getTopic() {
        return topic;
    }
}
