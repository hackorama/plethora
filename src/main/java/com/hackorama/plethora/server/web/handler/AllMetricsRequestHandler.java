package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.annotations.WebApiDoc;

@WebApiDoc(uri = "/getall", doc = "Returns all the metrics")
public class AllMetricsRequestHandler extends DataRequestHandler {

    private static final String NAME = "getall";

    public AllMetricsRequestHandler(MetricService metricService, Formatters formatterManager) {
        super(metricService, formatterManager);
    }

    @Override
    public byte[] getResponse(String param, com.hackorama.plethora.server.web.handler.DATA_TYPE type)
            throws UnsupportedEncodingException {
        Map<String, Object> metrics = metricService.getMetrics(param);
        return formatResponse(metrics, type).getBytes(Util.getEncoding());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private String formatResponse(Map<String, Object> map, DATA_TYPE type) {
        Formatter formatter = getFormatter(type);
        if (formatter != null) {
            return formatter.formatMetrics(map);
        }
        return map.toString();
    }

}
