package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;

import com.hackorama.plethora.server.MetricService;

public abstract class DataRequestHandler implements RequestHandler {

    protected final MetricService metricService;
    protected final Formatters formatterManager;

    public DataRequestHandler(MetricService metricService, Formatters formatterManager) {
        this.metricService = metricService;
        this.formatterManager = formatterManager;
    }

    @Override
    public abstract byte[] getResponse(String param, DATA_TYPE type) throws UnsupportedEncodingException;

    @Override
    public abstract String getName();

    @Override
    public byte[] getResponse(String param) throws UnsupportedEncodingException {
        return getResponse(param, DATA_TYPE.defaultDataType());
    }

    protected String formatResponse(String response, DATA_TYPE type) {
        return formatterManager.getformatter(type).format(response);
    }

    protected Formatter getFormatter(DATA_TYPE type) {
        return formatterManager.getformatter(type);
    }
}
