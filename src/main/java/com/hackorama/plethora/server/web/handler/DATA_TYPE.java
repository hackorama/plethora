package com.hackorama.plethora.server.web.handler;

public enum DATA_TYPE {
    JSON, TXT, CSV, XML, SITESCOPE;

    public static DATA_TYPE defaultDataType() {
        return TXT;
    }
}
