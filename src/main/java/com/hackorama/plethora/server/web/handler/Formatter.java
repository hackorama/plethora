package com.hackorama.plethora.server.web.handler;

import java.util.Map;
import java.util.Set;

public interface Formatter {
    DATA_TYPE type();

    String format(String input);

    String formatMetric(String name, String value);

    String formatModule(String name, Map<String, Object> map);

    String formatMetrics(Map<String, Object> map);

    String formatMetricsList(Set<String> set);

    String formatModulesList(Set<String> set);

    String formatMetricProps(String name, Map<String, String> map);

    String formatMetricsProps(Map<String, String> map);

}
