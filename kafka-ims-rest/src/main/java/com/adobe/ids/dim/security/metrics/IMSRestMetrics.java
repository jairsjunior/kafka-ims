/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.metrics;

import com.adobe.ids.dim.security.entity.RequestInfo;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMSRestMetrics {

    private static final Logger log = LoggerFactory.getLogger(IMSRestMetrics.class);
    private static IMSRestMetrics metrics;
    private Map<String, IMSRequestMetrics> requestMetricsMap;
    private Map<String, IMSACLMetrics> aclMetricsMap;
    private String prefix;

    public static IMSRestMetrics getInstance() {
        if(metrics == null) {
            metrics = new IMSRestMetrics("kafka.rest");
        }
        return metrics;
    }

    public IMSRestMetrics(String prefix) {
        this.requestMetricsMap = new HashMap<>();
        this.aclMetricsMap = new HashMap<>();
        this.prefix = prefix;
        checkAndCreateRequestMetric("total-request-metrics", "ims-request-metrics-total");
        checkAndCreateACLMetric("total-acl-metrics", "ims-acl-metrics-total");
    }

    public void addMetric(String uniqueName, String group, String key, String value) {
        if(!requestMetricsMap.containsKey(uniqueName)) {
            try {
                StringBuilder str = new StringBuilder(this.prefix);
                str.append(":type=").append(group);
//                        .append(",name=").append(name);
                if(key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    str.append(",").append(key).append("=").append(value);
                }
                ObjectName objectName = new ObjectName(str.toString());
                IMSRequestMetrics metric = new IMSRequestMetrics();
                ManagementFactory.getPlatformMBeanServer().registerMBean(metric, objectName);
                requestMetricsMap.put(uniqueName, metric);
            } catch (MalformedObjectNameException e) {
                log.error("Add Metrics ---", e);
            } catch (Exception e) {
                log.error("Add Metrics ---", e);
            }
        }
    }

    public void addMetricACL(String uniqueName, String group, String topic, String key, String value) {
        if(!requestMetricsMap.containsKey(uniqueName)) {
            try {
                StringBuilder str = new StringBuilder(this.prefix);
                str.append(":type=").append(group);
                if(topic != null) {
                    str.append(",topic=").append(topic);
                }
                if(key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    str.append(",").append(key).append("=").append(value);
                }
                ObjectName objectName = new ObjectName(str.toString());
                IMSACLMetrics metric = new IMSACLMetrics();
                ManagementFactory.getPlatformMBeanServer().registerMBean(metric, objectName);
                aclMetricsMap.put(uniqueName, metric);
            } catch (MalformedObjectNameException e) {
                log.error("Add Metrics ---", e);
            } catch (Exception e) {
                log.error("Add Metrics ---", e);
            }
        }
    }

    public IMSRequestMetrics getMetricsObject(String uniqueName) {
        return requestMetricsMap.get(uniqueName);
    }

    public IMSACLMetrics getACLMetricsObject(String uniqueName) {
        return aclMetricsMap.get(uniqueName);
    }

    private String mountUniqueName(RequestInfo req, String topic) {
        return new StringBuilder(req.getEndpoint()).append("-").append(topic).toString();
    }

    private String checkAndCreateByContext(ContainerRequestContext context, ResourceInfo resourceInfo) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, null);
        String uniqueName = mountUniqueName(req, "");
        if(!requestMetricsMap.containsKey(uniqueName)) {
            addMetric(uniqueName.toString(), "ims-metrics", "endpoint", req.getEndpoint());
        }
        return uniqueName;
    }

    private String checkAndCreateRequestMetric(String uniqueName, String name) {
        if(!requestMetricsMap.containsKey(uniqueName)) {
            addMetric(uniqueName.toString(), "ims-metrics", "name", name);
        }
        return uniqueName;
    }

    private String checkAndCreateByContextACL(ContainerRequestContext context, ResourceInfo resourceInfo, String topic) {
        RequestInfo req = OAuthRestProxyUtil.mountResquestInfo(context, resourceInfo, null);
        String uniqueName = mountUniqueName(req, topic);
        if(!aclMetricsMap.containsKey(uniqueName)) {
            addMetricACL(uniqueName.toString(), "ims-acl-metrics", topic, "endpoint", req.getEndpoint());
        }
        return uniqueName;
    }

    private String checkAndCreateACLMetric(String uniqueName, String name) {
        if(!aclMetricsMap.containsKey(uniqueName)) {
            addMetricACL(uniqueName.toString(), "ims-acl-metrics", null, "name", name);
        }
        return uniqueName;
    }

    public void incExpiredToken(ContainerRequestContext context, ResourceInfo resourceInfo) {
        String uniqueName = checkAndCreateByContext(context, resourceInfo);
        getMetricsObject(uniqueName).incCountOfRequestsFailedExpiredToken();
        getMetricsObject("total-request-metrics").incCountOfRequestsFailedExpiredToken();
    }

    public void incInvalidToken(ContainerRequestContext context, ResourceInfo resourceInfo) {
        String uniqueName = checkAndCreateByContext(context, resourceInfo);
        getMetricsObject(uniqueName).incCountOfRequestFailedInvalidToken();
        getMetricsObject("total-request-metrics").incCountOfRequestFailedInvalidToken();
    }

    public void incWithoutScope(ContainerRequestContext context, ResourceInfo resourceInfo) {
        String uniqueName = checkAndCreateByContext(context, resourceInfo);
        getMetricsObject(uniqueName).incCountOfRequestsFailedWithoutScope();
        getMetricsObject("total-request-metrics").incCountOfRequestsFailedWithoutScope();
    }

    public void incSucessfull(ContainerRequestContext context, ResourceInfo resourceInfo) {
        String uniqueName = checkAndCreateByContext(context, resourceInfo);
        getMetricsObject(uniqueName).incCountOfRequestSuccess();
        getMetricsObject("total-request-metrics").incCountOfRequestSuccess();
    }

    public void incACLDenied(ContainerRequestContext context, ResourceInfo resourceInfo, List<String> topics) {
        for(String topic : topics) {
            String uniqueName = checkAndCreateByContextACL(context, resourceInfo, topic);
            getACLMetricsObject(uniqueName).incACLDeniedACLRequestCount();
            getACLMetricsObject("total-acl-metrics").incACLDeniedACLRequestCount();
        }
    }
}
