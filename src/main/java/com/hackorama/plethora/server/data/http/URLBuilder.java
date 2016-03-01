package com.hackorama.plethora.server.data.http;

import com.hackorama.plethora.common.Log;

/**
 * A helper class that builds the URL patterns for each web proxy request to a remote plethora metric served through
 * HTTPS
 * 
 * Matches the URL pattern defined in plethoralib/base_metrics.py used by web metrics
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class URLBuilder {

    private final String url;
    // web proxy url patterns - matches Metrics (plethoralib/metrics.py)
    private static final String ARG_GET_METRIC = "get";
    private static final String ARG_SET_METRIC = "set";
    private static final String ARG_METRIC_VALUE = "with";
    private static final String ARG_METRIC_PROPS = "props";
    private static final String ARG_LIST = "list";
    private static final String ARG_LIST_METRICS = "metrics";
    private static final String ARG_LIST_METRIC_NAMES = "names";
    private static final String ARG_LIST_METRIC_PROPS = "props";

    public URLBuilder(String url) {
        if (url == null) {
            throw new IllegalArgumentException();
        }
        this.url = url;
    }

    private String buildRequest(String name, String value) {
        String result = new StringBuilder().append(url).append('?').append(name).append('=').append(value).toString();
        Log.getLogger().fine("Metric request URL" + result);
        return result;
    }

    private String buildListRequest(String name) {
        return buildRequest(ARG_LIST, name);
    }

    private String addRequest(String request, String name, String value) {
        String result = new StringBuilder().append(request).append('&').append(name).append('=').append(value)
                .toString();
        Log.getLogger().fine("Metric request URL" + result);
        return result;
    }

    public String buildGetMetricRequest(String metric) {
        return buildRequest(ARG_GET_METRIC, metric);
    }

    public String buildListMetricsRequest() {
        return buildListRequest(ARG_LIST_METRICS);
    }

    public String buildListNamesRequest() {
        return buildListRequest(ARG_LIST_METRIC_NAMES);
    }

    public String buildListPropsRequest() {
        return buildListRequest(ARG_LIST_METRIC_PROPS);
    }

    public String buildMetricPropsRequest(String metric) {
        return buildRequest(ARG_METRIC_PROPS, metric);
    }

    public String buildSetMetricRequest(String metric, String value) {
        return addRequest(buildRequest(ARG_SET_METRIC, metric), ARG_METRIC_VALUE, value);
    }

    public String buildSetMetricRequest(String metric, long value) {
        return buildSetMetricRequest(metric, String.valueOf(value));
    }

    public String buildConnectionStatus() {
        return url;
    }
}
