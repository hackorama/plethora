package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.annotations.WebApiDoc;

@WebApiDoc(uri = "/listmodules", doc = "Returns a list of the modules")
public class ListModulesRequestHandler extends DataRequestHandler {

    private static final String NAME = "listmodules";

    public ListModulesRequestHandler(MetricService metricService, Formatters formatterManager) {
        super(metricService, formatterManager);
    }

    @Override
    public byte[] getResponse(String param, DATA_TYPE type) throws UnsupportedEncodingException {
        Set<String> modules = metricService.getModuleList();
        return formatResponse(modules, type).getBytes(Util.getEncoding());
    }

    private String formatResponse(Set<String> list, DATA_TYPE type) {
        Formatter formatter = getFormatter(type);
        if (formatter != null) {
            return formatter.formatModulesList(list);
        }
        return list.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }

}
