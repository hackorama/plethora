package com.hackorama.plethora.server.web.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SimpleJSONFormatter implements Formatter {

    @Override
    public DATA_TYPE type() {
        return DATA_TYPE.JSON;
    }

    @Override
    public String format(String text) {
        return jsonBegin() + jsonProperty(text, text) + jsonEnd();
    }

    @Override
    public String formatMetric(String name, String value) {
        return jsonBegin() + jsonProperty(name, value) + jsonEnd();
    }

    @Override
    public String formatModule(String name, Map<String, Object> map) {
        return jsonFormatMap(map);
    }

    @Override
    public String formatMetrics(Map<String, Object> map) {
        return "TODO";
    }

    @Override
    public String formatMetricsList(Set<String> set) {
        return jsonFormatList("metrics", set);
    }

    @Override
    public String formatModulesList(Set<String> set) {
        return jsonFormatList("modules", set);
    }

    @Override
    public String formatMetricProps(String name, Map<String, String> map) {
        return "TODO";
    }

    @Override
    public String formatMetricsProps(Map<String, String> map) {
        return "TODO";
    }

    /*
     * Why I am even doing this, must use a JSON library
     */

    private String jsonFormatMap(Map<String, Object> map) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(jsonBegin());
        for (Entry<String, Object> entry : map.entrySet()) {
            buffer.append(jsonProperty(entry.getKey(), entry.getValue().toString()));
            buffer.append(jsonValueSeparator());
            buffer.append(jsonLineFeed());
        }
        buffer.append(jsonEnd());
        return buffer.toString();
    }

    private String jsonFormatList(String name, Set<String> list) {
        return jsonBegin() + jsonQuote(name) + jsonPropNameValueSeparator() + jsonArray(list) + jsonEnd();
    }

    private String jsonArray(Set<String> list) {
        StringBuffer buffer = new StringBuffer();
        for (String item : list) {
            buffer.append(item);
            buffer.append(jsonValueSeparator());
        }
        // remove the last separator and surround with array marker
        String items = buffer.substring(0, buffer.lastIndexOf(jsonValueSeparator()));
        buffer = new StringBuffer();
        buffer.append(jsonArrayBegin());
        buffer.append(items);
        buffer.append(jsonArrayEnd());
        return buffer.toString();
    }

    private String jsonProperty(String name, String value) {
        return jsonQuote(name) + jsonPropNameValueSeparator() + jsonQuote(value);
    }

    private String jsonBegin() {
        return jsonEntryBegin() + jsonLineFeed();
    }

    private String jsonEnd() {
        return jsonLineFeed() + jsonEntryEnd();
    }

    private String jsonLineFeed() {
        return System.getProperty("line.separator");
    }

    private String jsonEntryBegin() {
        return "{";
    }

    private String jsonEntryEnd() {
        return "}";
    }

    private String jsonArrayBegin() {
        return "[ ";
    }

    private String jsonArrayEnd() {
        return " ]";
    }

    private String jsonPropNameValueSeparator() {
        return " : ";
    }

    private String jsonValueSeparator() {
        return ", ";
    }

    private String jsonQuote(String text) {
        return "\"" + text + "\"";
    }

}
