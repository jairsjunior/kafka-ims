package com.adobe.ids.dim.security.metrics;

import io.confluent.common.metrics.*;
import io.confluent.common.metrics.stats.Count;
import io.confluent.common.utils.MockTime;
import io.confluent.kafkarest.Time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestConfluentMetricsClass {

    private static TestConfluentMetricsClass testMetrics;
    private Metrics metrics;
    private Map<String, Sensor> sensors;

    public static TestConfluentMetricsClass getInstance(){
        if(testMetrics == null){
            testMetrics = new TestConfluentMetricsClass();
        }
        return testMetrics;
    }

    public TestConfluentMetricsClass() {
        metrics = new Metrics(new MetricConfig(), Arrays.asList((MetricsReporter) new JmxReporter("kafka.rest")), new MockTime());
        sensors = new HashMap<>();
    }

//    public void addSensorCount(String sensorName, String sensorGroup, String... keyValue){
//        addSensorCount(sensorName, sensorGroup, "", keyValue);
//    }

    public void addSensorCount(String sensorName, String sensorGroup, String description, String... keyValue){
        Sensor s = metrics.sensor(sensorName);
        s.add(new MetricName(sensorName, sensorGroup, description, keyValue), new Count());
        sensors.put(sensorName, s);
    }

    public Sensor getSensor(String sensorName){
        return sensors.get(sensorName);
    }
}
