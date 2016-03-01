package com.hackorama.plethora.server.data.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC;
import com.hackorama.plethora.common.OptionString;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.server.data.common.JSONParser;
import com.hackorama.plethora.server.data.common.RemoteModule;

public final class WebModule extends RemoteModule {

    private final URLBuilder urlBuilder;
    private final Connector connector;

    // property names - matches Plethora (plethoralib/plethora.py)
    private static final String PROP_DISPLAYNAME = "displayname";
    private static final String PROP_OPTIONS = "options";
    private static final String PROP_DESCRIPTION = "description";

    public WebModule(String name, Metrics metrics, Connector connector) {
        super(metrics, name);
        this.connector = connector;
        urlBuilder = connector.getUrlBuilder();
        connect();
    }

    @Override
    protected boolean connectionStatus() {
        // connection status check, do not retry request on failure
        String response = remoteRequestNoRetry(urlBuilder.buildConnectionStatus());
        return response == null ? false : response.contains("204");
    }

    @Override
    protected void discoverMetrics() {
        List<String> names = getMetricNamesList();
        for (String metricname : names) {
            MetricProperties properties = getMetricProperties(metricname);
            if (properties == null) {
                Log.getLogger().severe("No metric properties discovered for " + metricname + " of " + moduleName);
                continue;
            }
            addMetric(metricname, properties);
        }
    }

    @Override
    protected Object getRemoteGenericMetric(String metric) {
        return JSONParser.getJsonValueObject(remoteRequest(urlBuilder.buildGetMetricRequest(metric)), metric);
    }

    @Override
    protected boolean setRemoteValueByName(String metric, String value) {
        String result = JSONParser.getJsonValue(remoteRequest(urlBuilder.buildSetMetricRequest(metric, value)), metric);
        return value.equals(result);
    }

    @Override
    protected boolean setRemoteValueByName(String metric, long value) {
        Long result = JSONParser.getJsonValueLong(remoteRequest(urlBuilder.buildSetMetricRequest(metric, value)),
                metric);
        return result == null ? false : result.longValue() == value;
    }

    @Override
    protected boolean setRemoteValueByName(String metric, boolean value) {
        Boolean result = JSONParser.getJsonValueBoolean(
                remoteRequest(urlBuilder.buildSetMetricRequest(metric, String.valueOf(value))), metric);
        return result == null ? false : result.booleanValue() == value;
    }

    private MetricProperties getMetricProperties(String metric) {
        String response = remoteRequest(urlBuilder.buildMetricPropsRequest(metric));
        if (response != null) {
            String displayname = JSONParser.getJsonValue(response, PROP_DISPLAYNAME);
            String description = JSONParser.getJsonValue(response, PROP_DESCRIPTION);
            String options = JSONParser.getJsonValue(response, PROP_OPTIONS);
            METRIC.LEVEL level = OptionString.getLevel(options);
            METRIC.TYPE type = OptionString.getType(options);
            boolean write = OptionString.getWritable(options);
            boolean read = true; // must be readable
            return new MetricProperties().displayname(displayname).description(description).level(level).readable(read)
                    .writable(write).type(type);
        }
        return null;
    }

    private List<String> getMetricNamesList() {
        List<String> names = new ArrayList<String>();
        JSONObject json = JSONParser.getJson(remoteRequest(urlBuilder.buildListNamesRequest()));
        if (json == null) {
            Log.getLogger().warning("Failed gettting names for " + moduleName);
        } else {
            try {
                JSONArray jsonArray = json.getJSONArray("plethora");
                for (int i = 0; i < jsonArray.length(); i++) {
                    names.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                Log.getLogger().warning("No names" + e.getMessage());
                Log.getLogger().log(Level.FINE, "JSON error", e);
                names.clear(); // reset to empty list on any exception
            }
        }
        return names;
    }

    private String remoteRequest(String url) {
        return remoteRequest(url, true);
    }

    private String remoteRequest(String url, boolean retry) {
        try {
            return connector.getResponse(url);
        } catch (IOException e) {
            if (retry) {
                Log.getLogger().warning(
                        "Connection failed for " + moduleName + ", " + e.getMessage() + ", Retrying ...");
                reconnect(); // retry failed connection
            }
        }
        Log.getLogger().warning("Failed getting metric from " + moduleName);
        return null;
    }

    private String remoteRequestNoRetry(String url) {
        return remoteRequest(url, false);
    }

}
