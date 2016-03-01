package com.hackorama.plethora.common.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

public class JMXAgent {
    private static final String WINDOWS_JMX_HELP = "On windows try using -Djava.rmi.server.hostname=localhost -Djava.rmi.server.useLocalHostname=true";
    protected MBeanServer mbeanServer;
    protected JMXConnectorServer connectorServer;
    private final PlethoraMBean mbean;
    protected final String mbeanName;
    protected final String moduleName;
    protected final String host;
    protected final int port;
    private boolean ready;
    protected final JMXResolver resolver;

    public JMXAgent(String moduleName, PlethoraMBean mbean, String host, int port) {
        ready = false;
        this.moduleName = moduleName;
        this.mbean = mbean;
        this.host = host;
        this.port = port;
        resolver = new JMXResolver();
        mbeanName = resolver.moduleJMXName(this.moduleName);
    }

    public boolean start() {
        if (validateParams() && createAgent()) {
            ready = registerMbean();
        }
        return isReady();
    }

    public String getMbeanName() {
        return mbeanName;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean updateNotify() {
        return refreshMbeanInfo();
    }

    protected boolean refreshMbeanInfo() {
        return mbean == null ? false : mbean.refreshMBeanInfo();
    }

    protected Map<String, Object> getEnv() {
        Map<String, Object> env = new HashMap<String, Object>();
        /*
         * Set up any additional RMI options here.
         * 
         * SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory(); SslRMIServerSocketFactory ssf = new
         * SslRMIServerSocketFactory();
         * 
         * env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
         * env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
         * 
         * System.setProperty("com.sun.management.jmxremote.authenticate", "true");
         * System.setProperty("com.sun.management.jmxremote.ssl", "true");
         * System.setProperty("com.sun.management.jmxremote.ssl.need.client.auth" * , "true");
         * 
         * Crypto.initSSL();
         */
        return env;
    }

    protected boolean createAgent() {
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Map<String, Object> env = getEnv();
        JMXServiceURL url = getURL();
        if (url == null) {
            return false;
        }
        String msg = moduleName + " at " + host + ":" + port + " (" + url + ")";
        try {
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbeanServer);
            connectorServer.start();
        } catch (BindException e) {
            Log.getLogger().log(Level.SEVERE, "Bind error, " + msg, e);
            Log.getLogger().info(WINDOWS_JMX_HELP);
            return false;
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "IO error, " + msg, e);
            return false;
        }

        Log.getLogger().info("JMX agent started for " + msg);
        return true;
    }

    protected boolean registerMbean() {
        ObjectName oname = null;
        try {
            oname = new ObjectName(mbeanName);
        } catch (MalformedObjectNameException e) {
            Log.getLogger().log(Level.SEVERE, "Bad object name for mbean, cannot register mbean", e);
            return false;
        } catch (NullPointerException e) {
            Log.getLogger().log(Level.SEVERE, "Null object name, cannot register mbean", e);
            return false;
        }
        try {
            mbeanServer.registerMBean(mbean, oname);
        } catch (InstanceAlreadyExistsException e) {
            Log.getLogger().log(Level.SEVERE, "Mbean instance already exists, cannot register mbean", e);
            return false;
        } catch (MBeanRegistrationException e) {
            Log.getLogger().log(Level.SEVERE, "Cannot register mbean", e);
            return false;
        } catch (NotCompliantMBeanException e) {
            Log.getLogger().log(Level.SEVERE, "Mbean is not compliant, cannot register mbean", e);
            return false;
        }
        return true;
    }

    protected JMXServiceURL getURL() {
        try {
            return new JMXServiceURL(resolver.jmxRmiServiceUrl(host, port));
        } catch (MalformedURLException e) {
            Log.getLogger().log(Level.SEVERE, "Bad URL for " + moduleName, e);
        }
        return null;
    }

    private boolean validateParams() {
        if (Util.invalidEmpty(mbeanName) || Util.invalidEmpty(host) || Util.invalidPort(port) || mbean == null) {
            Log.getLogger().severe(
                    "Illegal arguments, host=" + host + ", port=" + port + "mbean="
                            + (mbean == null ? ", null" : "valid") + ", name=" + mbeanName);
            return false;
        }
        return true;
    }
}
