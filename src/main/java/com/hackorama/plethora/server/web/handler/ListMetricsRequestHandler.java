package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.annotations.WebApiDoc;

@WebApiDoc(uri = "/listmetrics", doc = "Returns a list of the metrics")
public class ListMetricsRequestHandler extends DataRequestHandler {

    public ListMetricsRequestHandler(MetricService metricService, Formatters formatterManager) {
        super(metricService, formatterManager);
    }

    private static final String NAME = "listmetrics";

    @Override
    public byte[] getResponse(String param, DATA_TYPE type) throws UnsupportedEncodingException {
        Set<String> names = null;
        if (param.isEmpty()) {
            names = metricService.getMetricList();
        } else {
            names = metricService.getMetricList(param);
        }
        return formatResponse(names, type).getBytes(Util.getEncoding());
    }

    private String formatResponse(Set<String> list, DATA_TYPE type) {
        Formatter formatter = getFormatter(type);
        if (formatter != null) {
            return formatter.formatMetricsList(list);
        }
        return list.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }

}
