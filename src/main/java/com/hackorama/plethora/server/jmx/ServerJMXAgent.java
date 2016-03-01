package com.hackorama.plethora.server.jmx;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.jmx.PlethoraMBean;
import com.hackorama.plethora.common.jmx.JMXAgent;
import com.hackorama.plethora.server.MetricServer;

public final class ServerJMXAgent extends JMXAgent implements MetricServer, Callable<Object> {

    private static final String NAME = "JMX agent server";
    private boolean forceMBeanInfoUpdate;

    public ServerJMXAgent(String moduleName, PlethoraMBean mbean, String host, int port) {
        super(moduleName, mbean, host, port);
        forceMBeanInfoUpdate = false;
    }

    public void setUpdateMode(boolean forceMBeanInfoUpdate) {
        this.forceMBeanInfoUpdate = forceMBeanInfoUpdate;
    }

    @Override
    public boolean start() {
        String msg = NAME + " at " + host + ":" + port;
        if (super.start()) {
            Log.getLogger().info("Started " + msg);
            return true;
        }
        Log.getLogger().info("Failed to start " + msg);
        return false;
    }

    @Override
    public void stop() {
        unRegisterMbean();
        try {
            connectorServer.stop();
            Log.getLogger().info("Stopped " + NAME);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "Failed to stop " + NAME + ", IO error", e);
        }
    }

    @Override
    public Object call() throws Exception {
        return start();
    }

    @Override
    public boolean updateNotify() {
        /*
         * NOTE: re-register is a hack for refreshing JMX clients that cache the MBeanInfo without respecting
         * immutableInfo=false [ Please see : http://weblogs.java.net/blog/emcmanus/archive/2006/11/a_real_example
         * .html#comment-15322 ] Clients that do not cache MBeaninfo will only need MBeanInfo refresh
         */
        Log.getLogger().info("Updating " + NAME);
        if (forceMBeanInfoUpdate) {
            return reRegisterMbean() && refreshMbeanInfo();
        } else {
            return refreshMbeanInfo();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected boolean createAgent() {
        if (createRegistry()) {
            return super.createAgent();
        }
        return false;
    }

    protected boolean reRegisterMbean() {
        if (!unRegisterMbean()) {
            return false;
        }
        return registerMbean();
    }

    private boolean unRegisterMbean() {
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
            mbeanServer.unregisterMBean(oname);
        } catch (MBeanRegistrationException e) {
            Log.getLogger().log(Level.SEVERE, "Error unregistering mbean", e);
        } catch (InstanceNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "MBean instance not found", e);
        }
        return true;
    }

    private boolean createRegistry() {
        System.setProperty("java.rmi.server.randomIDs", "true");
        try {
            LocateRegistry.createRegistry(port);
            return true;
        } catch (RemoteException e) {
            Log.getLogger().log(Level.SEVERE, "Registering failed", e);
        }
        return false;
    }
}
