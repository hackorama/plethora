package com.hackorama.plethora.server.web.handler;

import java.util.Map;
import java.util.Set;

public class XMLFormatter implements Formatter {

    private static final String EOL = System.getProperty("line.separator");

    @Override
    public DATA_TYPE type() {
        return DATA_TYPE.XML;
    }

    @Override
    public String format(String text) {
        return xmlString(text, text);
    }

    @Override
    public String formatMetric(String name, String value) {
        return xmlString(name, value);
    }

    @Override
    public String formatModule(String name, Map<String, Object> map) {
        return xmlString(name, map);
    }

    @Override
    public String formatMetrics(Map<String, Object> map) {
        return xmlString("metrics", map);
    }

    @Override
    public String formatMetricsList(Set<String> set) {
        return xmlString("metrics", set);
    }

    @Override
    public String formatModulesList(Set<String> set) {
        return xmlString("modules", set);
    }

    @Override
    public String formatMetricProps(String name, Map<String, String> map) {
        return xmlString(name, map);
    }

    @Override
    public String formatMetricsProps(Map<String, String> map) {
        return xmlString("propertees", map);
    }

    private String xmlString(String name, String value) {
        String result = "<xml comment=\"TODO : XML formatter implemention\">" + EOL;
        result += value;
        return result + EOL + "</xml>" + EOL;
    }

    private String xmlString(String name, Map<String, ?> data) {
        String result = "<xml comment=\"TODO : XML formatter implemention\">" + EOL;
        result += data.toString();
        return result + EOL + "</xml>" + EOL;
    }

    private String xmlString(String name, Set<String> data) {
        String result = "<xml comment=\"TODO : XML formatter implemention\">" + EOL;
        result += data.toString();
        return result + EOL + "</xml>" + EOL;
    }
}
