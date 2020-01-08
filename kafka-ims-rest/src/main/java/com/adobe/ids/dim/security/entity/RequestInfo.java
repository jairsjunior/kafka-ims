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

    public String getEndpoint(){
        return endpoint;
    }
}
