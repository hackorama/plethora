package com.hackorama.plethora.server.web.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hackorama.plethora.common.Log;

public class Formatters {

    public static final Map<DATA_TYPE, Formatter> FORMATTERS = new HashMap<DATA_TYPE, Formatter>();

    public final void addFormatter(Formatter formatter) {
        Log.getLogger().finest("Adding response formatter of type " + formatter.type());
        FORMATTERS.put(formatter.type(), formatter);
    }

    public Set<DATA_TYPE> getKnownDataTypes() {
        return FORMATTERS.keySet();
    }

    public Formatter getformatter(DATA_TYPE type) {
        return FORMATTERS.get(type);
    }

    public Formatters() {
        initFormatters();
    }

    private final void initFormatters() {
        addFormatter(new JSONFormatter());
        addFormatter(new XMLFormatter());
        addFormatter(new SiteScopeXMLFormatter());
    }
}
