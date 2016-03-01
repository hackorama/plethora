package com.hackorama.plethora.server.data.common;

import com.hackorama.plethora.common.data.Metrics;

/**
 * This is a specialized base module that holds metric collections that are collected local to the Plethora server. Like
 * system metrics that are not specific to an application module but applies to all application modules in the system,
 * and cluster metrics that applies to all application cluster members.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public abstract class LocalModule extends Module {

    public LocalModule(Metrics metrics, String moduleName) {
        super(metrics, moduleName);
        connected = true; // local is always connected
    }

    @Override
    public boolean setValue(String metric, String value) {
        return metrics.setValue(metric, value);
    }

    @Override
    public boolean setValue(String metric, long value) {
        return metrics.setValue(metric, value);
    }

    @Override
    public boolean setValue(String metric, boolean value) {
        return metrics.setValue(metric, value);
    }

    @Override
    public Object getValue(String metric) {
        return metrics.getValue(metric);
    }

    @Override
    public boolean setValue(String metricName, Object value) {
        return metrics.setValue(metricName, value);
    }

    @Override
    protected boolean connectionStatus() {
        return true; // local is always connected
    }

}
