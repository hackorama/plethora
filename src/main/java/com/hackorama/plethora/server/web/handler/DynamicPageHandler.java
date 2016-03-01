package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.server.MetricService;

public class DynamicPageHandler {

    private final Map<String, RequestHandler> HANDLERS = new HashMap<String, RequestHandler>();
    private static final Map<String, DATA_TYPE> DATA_TYPES = new HashMap<String, DATA_TYPE>();

    public DynamicPageHandler(MetricService metricService, Formatters formatters) {
        initHandlers(metricService, formatters);
        initDataTypes(formatters);
    }

    /**
     * Creates dynamic page content response based on the request URL path.
     * 
     * Process the path following the expected URL path convention specific to this handler as : /[type]/name/[param]
     * 
     * @param path
     *            The URL path from the HTTP request
     * @return The page response as a byte array
     * @throws UnsupportedEncodingException
     */
    public byte[] handleRequest(String path) {
        String requestType = null;
        String requestName = "";
        String requestParam = "";
        // remove leading '/' and get the path segments
        List<String> pathSegments = getSegmentedPath(path.substring(1));
        if (pathSegments.size() > 1) {
            // if first path segment is a known data type
            if (isKnownDataType(pathSegments.get(0))) {
                // 1. handle /type/name/[param]
                requestType = pathSegments.get(0);
                // handle rest of the path as /name/[param]
                String nextSubPath = pathSegments.get(1);
                List<String> nextPathSegments = getSegmentedPath(nextSubPath);
                requestName = nextPathSegments.get(0);
                if (nextPathSegments.size() > 1) {
                    requestParam = nextPathSegments.get(1);
                }
            } else { // 2. handle /name/param
                requestName = pathSegments.get(0);
                requestParam = pathSegments.get(1);
            }
        } else if (pathSegments.size() > 0) { // 3. handle /name
            requestName = pathSegments.get(0);
        }
        try {
            return getResponse(requestName, requestParam, requestType);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().log(Level.SEVERE, "Encoding error: " + requestName + ", " + requestParam, e);
            return "Encoding Error".getBytes(); // using default encoding
        }
    }

    public final void addDynamicHandler(RequestHandler handler) {
        Log.getLogger().finest("Adding dynamic web request handler for " + handler.getName());
        HANDLERS.put(handler.getName(), handler);
    }

    private List<String> getSegmentedPath(String path) {
        List<String> segments = new ArrayList<String>();
        Collections.addAll(segments, path.split("/", 2));
        return segments;
    }

    private byte[] getResponse(String requestName, String requestParam, String requestType)
            throws UnsupportedEncodingException {
        RequestHandler handler = getHandler(requestName);
        if (handler != null) {
            DATA_TYPE dataType = getRequestType(requestType);
            return dataType == null ? handler.getResponse(requestParam) : handler.getResponse(requestParam, dataType);
        }
        return null; // null (not empty array) to indicate no handler
    }

    private void initHandlers(MetricService metricService, Formatters formatterManager) {
        addDynamicHandler(new MetricRequestHandler(metricService, formatterManager));
        addDynamicHandler(new AllMetricsRequestHandler(metricService, formatterManager));
        addDynamicHandler(new ModuleMetricsRequestHandler(metricService, formatterManager));
        addDynamicHandler(new ListMetricsRequestHandler(metricService, formatterManager));
        addDynamicHandler(new ListModulesRequestHandler(metricService, formatterManager));
    }

    private void initDataTypes(Formatters formatterManager) {
        for (DATA_TYPE type : formatterManager.getKnownDataTypes()) {
            DATA_TYPES.put(type.name().toLowerCase(), type);
        }
    }

    private boolean isKnownDataType(String type) {
        if (type != null && DATA_TYPES.keySet().contains(type)) {
            return true;
        }
        return false;
    }

    private DATA_TYPE getRequestType(String type) {
        return DATA_TYPES.get(type);
    }

    private RequestHandler getHandler(String name) {
        return HANDLERS.get(name);
    }

}
