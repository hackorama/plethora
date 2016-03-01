package com.hackorama.plethora.server.web.handler;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import com.hackorama.plethora.common.Log;

public class JSONFormatter implements Formatter {

    private static final String ERROR_JSON = "{ \"error\": \"processing error, check logs\" }";

    @Override
    public DATA_TYPE type() {
        return DATA_TYPE.JSON;
    }

    @Override
    public String format(String text) {
        return jsonString(text, text);
    }

    @Override
    public String formatMetric(String name, String value) {
        return jsonString(name, value);
    }

    @Override
    public String formatModule(String name, Map<String, Object> map) {
        return jsonString(name, map);
    }

    @Override
    public String formatMetrics(Map<String, Object> map) {
        return jsonString("metrics", map);
    }

    @Override
    public String formatMetricsList(Set<String> set) {
        return jsonString("metrics", set);
    }

    @Override
    public String formatModulesList(Set<String> set) {
        return jsonString("modules", set);
    }

    @Override
    public String formatMetricProps(String name, Map<String, String> map) {
        return jsonString(name, map);
    }

    @Override
    public String formatMetricsProps(Map<String, String> map) {
        return jsonString("propertees", map);
    }

    private String jsonString(String name, String value) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, value);
        } catch (JSONException e) {
            Log.getLogger().log(Level.SEVERE, "JSON procesing error", e);
            return ERROR_JSON;
        }
        return json.toString();
    }

    private String jsonString(String name, Map<String, ?> data) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, data);
        } catch (JSONException e) {
            Log.getLogger().log(Level.SEVERE, "JSON procesing error", e);
            return ERROR_JSON;
        }
        return json.toString();
    }

    private String jsonString(String name, Set<String> data) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, data);
        } catch (JSONException e) {
            Log.getLogger().log(Level.SEVERE, "JSON procesing error", e);
            return ERROR_JSON;
        }
        return json.toString();
    }
}
