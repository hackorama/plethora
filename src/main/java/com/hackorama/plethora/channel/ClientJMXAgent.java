package com.hackorama.plethora.channel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Map;
import java.util.logging.Level;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ServerSocketFactory;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.jmx.PlethoraMBean;
import com.hackorama.plethora.common.jmx.JMXAgent;

public final class ClientJMXAgent extends JMXAgent {

    public ClientJMXAgent(String moduleName, PlethoraMBean mbean, String host, int port) {
        super(moduleName, mbean, host, port);
    }

    @Override
    protected JMXServiceURL getURL() {
        try {
            return new JMXServiceURL(resolver.jmxMpServiceUrl(host, port));
        } catch (MalformedURLException e) {
            Log.getLogger().log(Level.SEVERE, "Bad URL for " + moduleName, e);
        }
        return null;
    }

    @Override
    protected Map<String, Object> getEnv() {
        Map<String, Object> env = super.getEnv();
        RMIServerSocketFactory serverSocketFactory = new RestrictedRMIServerSocketFactory();
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverSocketFactory);
        return env;
    }

    /* restrict to local address binding only */
    private class RestrictedRMIServerSocketFactory implements RMIServerSocketFactory {
        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            return ServerSocketFactory.getDefault().createServerSocket(port, 0, resolver.resolveLocalServerAdress());
        }
    }

}
