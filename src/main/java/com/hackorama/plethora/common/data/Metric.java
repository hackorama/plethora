package com.hackorama.plethora.common.data;

import com.hackorama.plethora.common.METRIC;
import com.hackorama.plethora.common.Util;

public final class Metric<T> {

    private String name = null;
    private T value = null;
    private T initialValue = value;
    private T lastValidValue = value;
    private String metricName = name;
    private String moduleName = name;
    private METRIC.TYPE metricType = METRIC.TYPE.INVALID;
    private MetricProperties properties;
    private NameResolver resolver;

    public Metric(String name, T value) {
        this(name, value, new MetricProperties().displayname(name));
    }

    public Metric(String name, T value, MetricProperties properties) {
        init(name, value, properties);
    }

    public T getValue() {
        return value;
    }

    public boolean setValue(T value) {
        this.value = value;
        if (value == null) {
            return false;
        }
        this.lastValidValue = value;
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean setValueAsObject(Object value) {
        this.value = (T) value;
        if (value == null) {
            return false;
        }
        this.lastValidValue = (T) value;
        return true;
    }

    public T getInitialValue() {
        return initialValue;
    }

    public MetricProperties getProperties() {
        return properties;
    }

    public T getLastValidValue() {
        return lastValidValue;
    }

    public METRIC.LEVEL getLevel() {
        return properties.getLevel();
    }

    public String getType() {
        return lastValidValue == null ? null : lastValidValue.getClass().getCanonicalName();
    }

    public METRIC.TYPE getMetricType() {
        return this.metricType;
    }

    public METRIC.TYPE resolveMetricType() {
        if ("java.lang.Long".equals(getType())) {
            return METRIC.TYPE.NUMBER;
        } else if ("java.lang.String".equals(getType())) {
            return METRIC.TYPE.TEXT;
        } else if ("java.lang.Boolean".equals(getType())) {
            return METRIC.TYPE.BOOLEAN;
        }
        return METRIC.TYPE.INVALID;
    }

    public String getName() {
        return name;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDisplayName() {
        return properties.getDisplayname();
    }

    public String getDescription() {
        return properties.getDescription();
    }

    public boolean canRead() {
        return properties.isReadable();
    }

    public boolean canWrite() {
        return properties.isWritable();
    }

    public boolean canReadWrite() {
        return canRead() && canWrite();
    }

    public boolean readOnly() {
        return canRead() && !canWrite();
    }

    private void init(String name, T value) {
        if (Util.invalidEmpty(name)) {
            throw new IllegalArgumentException("Illegal name or value for metric");
        }
        this.name = name;
        this.value = value;
        initialValue = value;
        lastValidValue = value;
        properties = new MetricProperties();
        resolver = new NameResolver();
        metricName = resolver.resolveMetric(name);
        moduleName = resolver.resolveModule(name);
        metricType = resolveMetricType();
    }

    private void init(String name, T value, MetricProperties properties) {
        init(name, value);
        this.properties = properties;
    }

}
