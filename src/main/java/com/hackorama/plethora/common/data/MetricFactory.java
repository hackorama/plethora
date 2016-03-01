package com.hackorama.plethora.common.data;

import com.hackorama.plethora.common.Util;

public final class MetricFactory {

    private MetricFactory() {
        // no instances
    }

    public static Metric<?> getMetric(String name, MetricProperties properties) {
        return MetricFactory.getMetric(name, "", properties);
    }

    public static Metric<?> getMetric(String name, String textvalue, MetricProperties properties) {
        if (Util.invalidEmpty(name)) {
            return null;
        }
        switch (properties.getType()) {
        case NUMBER:
            return getMetric(name, getValidNumber(textvalue), properties);
        case TEXT:
            return new Metric<String>(name, getValidText(textvalue), properties);
        case BOOLEAN:
            return getMetric(name, getValidBoolean(textvalue), properties);
        default:
            return null;
        }
    }

    public static Metric<?> getMetric(String name, Long value, MetricProperties properties) {
        return Util.invalidEmpty(name) ? null : new Metric<Long>(name, value, properties);
    }

    public static Metric<?> getMetric(String name, Boolean value, MetricProperties properties) {
        return Util.invalidEmpty(name) ? null : new Metric<Boolean>(name, value, properties);
    }

    private static String getValidText(String value) {
        // null defaults to empty string ""
        return value == null ? "" : value;
    }

    private static Long getValidNumber(String value) {
        // null or invalid number defaults to 0
        return Util.getLong(value, 0);
    }

    private static boolean getValidBoolean(String value) {
        // only true or "1" qualifies as true all others default to false
        return "true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value);
    }

}
