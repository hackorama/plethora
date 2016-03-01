package com.hackorama.plethora.server.jmx;

import com.hackorama.plethora.common.jmx.PlethoraMBean;
import com.hackorama.plethora.server.MetricService;

public class PlethoraServerMBean extends PlethoraMBean {

    private final MetricService metricService;

    public PlethoraServerMBean(MetricService metricService) {
        super(metricService.getMetricsInstance());
        this.metricService = metricService;
    }

    @Override
    protected Object getMetric(String name) {
        return metricService.getMetric(name);
    }

    @Override
    protected boolean setMetricValue(String name, Object value) {
        return metricService.setMetricValue(name, value);
    }
}
