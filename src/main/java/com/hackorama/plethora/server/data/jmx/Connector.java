package com.hackorama.plethora.server.data.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ServerSocketFactory;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.jmx.JMXResolver;

enum ConnectionStrategy {
    NORMAL("normal"), CHECKED("checked"), CHECKED_OPTIMISTIC("checked.optimistic"), MANAGED("managed"), MANAGED_CONSERVATIVE(
            "managed.conservative");

    private final String name;

    private ConnectionStrategy(String name) {
        this.name = name;
    }

    public static ConnectionStrategy byName(String thename) {
        if (thename != null) {
            for (ConnectionStrategy strategy : ConnectionStrategy.values()) {
                if (thename.equalsIgnoreCase(strategy.name)) {
                    return strategy;
                }
            }
        }
        return null;
    }

    public static ConnectionStrategy defStrategy() {
        return NORMAL;
    }
}

/**
 * JMX connection provider with optional connection retry strategies to avoid any build up of pending JMX connection
 * threads
 * 
 * 1. Checked Connection strategy (ConnectionChecker)
 * 
 * JMX connection retry attempts are avoided by first checking for connection end point availability using a socket
 * connection attempt
 * 
 * Side effects: On the remote JMX server side you will see a "single" warning
 * 
 * -- exception warning for the checked connect: WARNING: Failed to open connection: java.net.SocketException: Software
 * caused connection abort: recv failed java.net.SocketException: Software caused connection abort: recv failed --
 * 
 * NOTE: Could be avoided by setting JMX error handling properties on the remote server
 * 
 * 2. Thread collection strategy (ConnectionThreadTracker)
 * 
 * The waiting JMX connection threads from the retry attempts are tracked and stopped as part of each retry attempt
 * 
 * Side effects: None, But the use of deprecated Thread.stop() is not the best solution
 * 
 * Both strategy also have the following variation.
 * 
 * 2. Optimistic checked
 * 
 * Allows first few connections to use direct connection before starting checked connections
 * 
 * 3. Conservative thread collection
 * 
 * Allows a grace period for waiting threads before collecting them.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class Connector {

    private final String name;
    private final String host;
    private final int port;
    private final JMXServiceURL jmxServiceUrl;
    private final ConnectionStrategy connectionStrategy;
    private final JMXResolver resolver;

    // connection failures for this connection end point
    private int failCount;
    // max allowed connection failures for this end point (optimistic checked)
    private static final int MAX_FAILURES = 3;
    // total connection failures for all end points in this app instance
    private static int totalFailCount;
    // max allowed total connection failures for all end points in app instance
    private static final int MAX_TOTAL_FAILURES = 30;
    // allowed grace period for connection threads in wait, before collecting
    private static final int ALLOW_WAIT_SECS = 60;

    public Connector(String name, String host, int port) {
        this(name, host, port, ConnectionStrategy.defStrategy());
    }

    public Connector(String name, String host, int port, ConnectionStrategy strategy) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.connectionStrategy = strategy;
        resolver = new JMXResolver();
        if (Util.invalidEmpty(name) || Util.invalidEmpty(host) || Util.invalidPort(port)) {
            Log.getLogger().severe("Invalid arguments to MBean server connection");
            jmxServiceUrl = null;
        } else {
            jmxServiceUrl = getServiceURL(host, port);
        }
    }

    public MBeanServerConnection connect() {
        if (jmxServiceUrl == null) {
            return null;
        }
        switch (connectionStrategy) { // TODO polymorphic or command map pattern
        case MANAGED:
            return makeManagedConnection();
        case MANAGED_CONSERVATIVE:
            return makeConservativeManagedConnection();
        case CHECKED:
            return makeCheckedConnection();
        case CHECKED_OPTIMISTIC:
            return makeOptimisticCheckedConnection();
        default:
            return makeConenction();
        }
    }

    public MBeanServerConnection reconnect() {
        // any additional housekeeping for reconnects here
        return connect();
    }

    private MBeanServerConnection makeManagedConnection() {
        return makeManagedConnection(0);
    }

    private MBeanServerConnection makeConservativeManagedConnection() {
        return makeManagedConnection(ALLOW_WAIT_SECS);
    }

    private MBeanServerConnection makeManagedConnection(int allowWaitTimeSecs) {
        MBeanServerConnection connection = makeConenction();
        if (null == connection) {
            ConnectionThreadTracker.reclaimWaiting(allowWaitTimeSecs);
        }
        return connection;
    }

    private MBeanServerConnection makeCheckedConnection() {
        if (!ConnectionChecker.isReachable(host, port)) {
            Log.getLogger().fine("Endpoint not reachable for jmx connection.");
            return null;
        }
        return makeConenction();
    }

    private MBeanServerConnection makeOptimisticCheckedConnection() {
        return withinOptimisticLimit() ? makeConenction() : makeCheckedConnection();
    }

    private boolean withinOptimisticLimit() {
        return failCount < MAX_FAILURES && totalFailCount < MAX_TOTAL_FAILURES;
    }

    private MBeanServerConnection makeConenction() {
        try {
            return JMXConnectorFactory.connect(jmxServiceUrl, buildEnv()).getMBeanServerConnection();
        } catch (IOException e) {
            failCount++;
            totalFailCount++;
            Log.getLogger().warning("JMX module " + name + " connection failed with, " + e.getMessage());
            Log.getLogger().fine("( " + name + " failed " + failCount + " of total " + totalFailCount + " failures)");
        }
        return null;
    }

    private JMXServiceURL getServiceURL(String host, int port) {
        try {
            return new JMXServiceURL(resolveJMXMProxyEndPointURL(host, port));
        } catch (MalformedURLException e) {
            Log.getLogger().log(Level.FINE, "Bad jmx url : " + resolveJMXMProxyEndPointURL(host, port), e);
        }
        return null;
    }

    private String resolveJMXMProxyEndPointURL(String host, int port) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resolver.getJMXProtocol());
        buffer.append(host);
        buffer.append(':');
        buffer.append(port);
        return buffer.toString();
    }

    private Map<String, Object> buildEnv() {
        HashMap<String, Object> env = new HashMap<String, Object>();

        /* add any additional properties to the connector */

        // RMIClientSocketFactory clientSocketFactory = new
        // RestrictedRMIClientSocketFactory();
        // env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
        // clientSocketFactory);

        // RMIServerSocketFactory serverSocketFactory = new
        // RestrictedRMIServerSocketFactory();
        // env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
        // serverSocketFactory);

        // env.put(Context.SECURITY_PRINCIPAL, username);
        // env.put(Context.SECURITY_CREDENTIALS, password);
        // env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "???");
        return env;
    }

    private static class RestrictedRMIClientSocketFactory implements RMIClientSocketFactory {
        private final JMXResolver resolver = new JMXResolver();

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return RMISocketFactory.getDefaultSocketFactory().createSocket(resolver.resolveLocalServerHostname(), port);

        }
    }

    private static class RestrictedRMIServerSocketFactory implements RMIServerSocketFactory {
        private final JMXResolver resolver = new JMXResolver();

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            return ServerSocketFactory.getDefault().createServerSocket(port, 0, resolver.resolveLocalServerAdress());
        }
    }

    public static void main(String[] argv) throws IOException, InterruptedException {
        Log.getLogger().setLevel(Level.FINE);
        MBeanServerConnection mbscOne = null, mbscTwo = null;
        Connector connectorOne = new Connector("one", "localhost", 9001);
        Connector connectorTwo = new Connector("two", "localhost", 9002);
        int count = 0;
        while (mbscOne == null || mbscTwo == null) {
            Log.getLogger().info("Connecting " + count++ + " ...");
            if (mbscOne == null) {
                mbscOne = connectorOne.connect();
            } else {
                Log.getLogger().info("Connected to one " + mbscOne.toString());
            }
            if (mbscTwo == null) {
                mbscTwo = connectorTwo.connect();
            } else {
                Log.getLogger().info("Connected to two " + mbscTwo.toString());

            }
            Thread.sleep(1000 * 5);
        }
        Log.getLogger().info("OK all connected");
    }

}
