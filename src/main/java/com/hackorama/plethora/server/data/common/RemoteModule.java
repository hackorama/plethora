package com.hackorama.plethora.server.data.common;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.data.Metrics;

public abstract class RemoteModule extends Module {

    protected RemoteModule() {
        // required implicit constructor
    }

    protected RemoteModule(Metrics metrics) {
        super(metrics);
    }

    protected RemoteModule(Metrics metrics, String name) {
        super(metrics, name);
    }

    @Override
    public Object getValue(String metric) {
        if (isValidMetricAndModuleConnected(metric)) {
            return getRemoteGenericMetric(metric);
        }
        return null;
    }

    @Override
    public boolean setValue(String metricName, Object value) {
        // TODO future extension for any object type
        return false;
    }

    @Override
    public boolean setValue(String metric, long value) {
        if (isValidMetricAndModuleConnected(metric)) {
            return setRemoteValueByName(metric, value);
        }
        return false;
    }

    @Override
    public boolean setValue(String metric, String value) {
        if (isValidMetricAndModuleConnected(metric) && value != null) {
            return setRemoteValueByName(metric, value);
        }
        return false;
    }

    @Override
    public boolean setValue(String metric, boolean value) {
        if (isValidMetricAndModuleConnected(metric)) {
            return setRemoteValueByName(metric, value);
        }
        return false;
    }

    protected abstract Object getRemoteGenericMetric(String metric);

    protected abstract boolean setRemoteValueByName(String metric, String value);

    protected abstract boolean setRemoteValueByName(String metric, long value);

    protected abstract boolean setRemoteValueByName(String metric, boolean value);

    protected boolean isValidMetricAndModuleConnected(String metric) {
        return Util.validNonEmpty(metric) ? isConnected() : false;
    }

}
