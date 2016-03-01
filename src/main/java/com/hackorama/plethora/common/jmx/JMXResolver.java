package com.hackorama.plethora.common.jmx;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hackorama.plethora.common.Util;

public final class JMXResolver {

    private static final String ROOT_JMX_NAME = "plethora";
    private static final String JMXRMI_PROTOCOL = "service:jmx:rmi:///jndi/rmi://";
    private static final String JMXMP_PROTOCOL = "service:jmx:jmxmp://";

    public JMXResolver() {

    }

    public String jmxMpServiceUrl(String host, int port) {
        return JMXMP_PROTOCOL + host + ":" + port;
    }

    public String jmxRmiServiceUrl(String host, int port) {
        return JMXRMI_PROTOCOL + host + ":" + port + "/jmxrmi";
    }

    public String moduleJMXName(String module) {
        if (Util.invalidEmpty(module)) {
            return null;
        }
        return ROOT_JMX_NAME + ":name=" + module;
    }

    public InetAddress resolveLocalServerAdress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    public String resolveLocalServerHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    public Object getJMXProtocol() {
        return JMXMP_PROTOCOL;
    }

}
