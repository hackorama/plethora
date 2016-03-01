package com.hackorama.plethora.common.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hackorama.plethora.common.Log;

public enum Metrics {
    INSTANCE;

    /*
     * ConcurrentHashMap is used for the metrics map (metricsMap) which gets accessed concurrently by the connection
     * manager thread who discovers metrics at runtime and updates the map with new metrics, and by the data refresh
     * thread who access the map to read/write the metrics values.
     */
    private static Map<String, Metric<?>> metricsMap = new ConcurrentHashMap<String, Metric<?>>();
    private static NameResolver resolver = new NameResolver();

    public static Metrics getInstance() {
        return INSTANCE;
    }

    public NameResolver getResolver() {
        return resolver;
    }

    public Object getValue(String name) {
        Metric<?> metric = getMetric(name);
        return metric == null ? null : metric.getValue();
    }

    public boolean setValue(String name, Object value) {
        Metric<?> metric = getMetric(name);
        if (metric != null) {
            @SuppressWarnings("unchecked")
            Metric<Object> themetric = (Metric<Object>) metric;
            return themetric.setValue(value);
        }
        return false;
    }

    public void addMetric(Metric<?> metric) {
        if (validMetric(metric)) {
            metricsMap.put(metric.getName(), metric);
        } else {
            Log.getLogger().severe("Failed to add invalid metric to metrics list");
        }
    }

    public boolean hasMetric(String name) {
        return metricsMap.containsKey(name);
    }

    public Map<String, Metric<?>> getMetrics() {
        return metricsMap;
    }

    public String getMetricType(String name) {
        Metric<?> metric = getMetric(name);
        return metric == null ? null : metric.getType();
    }

    public Set<String> getNames() {
        return metricsMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Metric<?> metric : metricsMap.values()) {
            if (validMetric(metric)) {
                buffer.append(metric.getName() + " = " + metric.getValue());
            } else {
                buffer.append("Invalid metric");
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public int size() {
        return metricsMap.size();
    }

    public Metric<?> getMetric(String name) {
        return metricsMap.get(name);
    }

    private boolean validMetric(Metric<?> metric) {
        if (metric != null && metric.getName() != null) {
            return true;
        }
        return false;
    }

}
