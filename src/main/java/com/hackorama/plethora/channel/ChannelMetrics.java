package com.hackorama.plethora.channel;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.data.Metrics;

/**
 * Define additional channel specific methods over the common Metrics
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class ChannelMetrics {

    private final Metrics metrics;

    public ChannelMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Long getNumberValue(String name) {
        if (metrics == null) {
            return null;
        }
        Object object = metrics.getValue(name);
        if (object instanceof Long) {
            return (Long) object;
        }
        // TODO PlethoraWrongMetricTypeException
        Log.getLogger().warning(name + " is not a number type metric");
        return null;
    }

    public String getTextValue(String name) {
        if (metrics == null) {
            return null;
        }
        Object object = metrics.getValue(name);
        if (object instanceof String) {
            return (String) object;
        }
        // TODO PlethoraWrongMetricTypeException
        Log.getLogger().warning(name + " is not a text type metric");
        return null;
    }

    /*
     * Using null return to indicate error, making this a three value boolean, and not proud of it. TODO
     */
    public Boolean getBooleanValue(String name) {
        if (metrics == null) {
            return null;
        }
        Object object = metrics.getValue(name);
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        Log.getLogger().warning(name + " is not a boolean type metric");
        return null;
    }
}
