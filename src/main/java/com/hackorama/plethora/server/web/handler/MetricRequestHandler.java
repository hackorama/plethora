package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.annotations.WebApiDoc;

@WebApiDoc(uri = "/get/{metric}", doc = "Returns the value for the metric")
public class MetricRequestHandler extends DataRequestHandler {

    public MetricRequestHandler(MetricService metricService, Formatters formatterManager) {
        super(metricService, formatterManager);
    }

    private static final String NAME = "get";
    private static final String DEFAULT = "null";

    @Override
    public byte[] getResponse(String param, DATA_TYPE type) throws UnsupportedEncodingException {
        return formatResponse(param, getValue(param), type).getBytes(Util.getEncoding());
    }

    public String getValue(String param) {
        Object object = null;
        try {
            object = metricService.getMetric(param);
        } catch (Exception e) {
            Log.getLogger().severe("Failed getting metric :" + param + ", " + e.getMessage());
        }
        return object == null ? DEFAULT : object.toString();
    }

    private String formatResponse(String name, String value, DATA_TYPE type) {
        Formatter formatter = getFormatter(type);
        if (formatter != null) {
            try {
                return formatter.formatMetric(name, value);
            } catch (Exception e) {
                Log.getLogger().severe("Error formatting response for metric " + name + ", " + e.getMessage());
            }
        }
        return value;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
