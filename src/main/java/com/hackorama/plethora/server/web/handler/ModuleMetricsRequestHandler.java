package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.annotations.WebApiDoc;

@WebApiDoc(uri = "/getallfor/{module}", doc = "Returns all the metrics for given module")
public class ModuleMetricsRequestHandler extends DataRequestHandler {

    private static final String NAME = "getallfor";

    public ModuleMetricsRequestHandler(MetricService metricService, Formatters formatterManager) {
        super(metricService, formatterManager);
    }

    @Override
    public byte[] getResponse(String param, DATA_TYPE type) throws UnsupportedEncodingException {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = metricService.getModuleMetrics(param);
        } catch (Exception e) {
            Log.getLogger().severe("Failed getting metrics for module :" + param + ", " + e.getMessage());
        }
        return formatResponse(param, result, type).getBytes(Util.getEncoding());
    }

    private String formatResponse(String name, Map<String, Object> result, DATA_TYPE type) {
        Formatter formatter = getFormatter(type);
        if (formatter != null) {
            try {
                return formatter.formatModule(name, result);
            } catch (Exception e) {
                Log.getLogger().severe("Error formatting response for module " + name + ", " + e.getMessage());
            }
        }
        return result.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }

}
