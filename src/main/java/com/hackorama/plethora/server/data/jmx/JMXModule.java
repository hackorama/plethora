package com.hackorama.plethora.server.data.jmx;

import java.io.IOException;
import java.util.logging.Level;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.METRIC.TYPE;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.common.jmx.JMXResolver;
import com.hackorama.plethora.server.data.common.RemoteModule;

public final class JMXModule extends RemoteModule {

    private final ObjectName mbeanName;
    private final Connector connector;
    private MBeanServerConnection serverConnection;
    private final JMXResolver jmxResolver;

    public JMXModule(Metrics metrics, String name, String host, int port) {
        super(metrics, name);
        if (!Util.validPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        jmxResolver = new JMXResolver();
        mbeanName = getMBeanObjectName();
        connector = new Connector(name, host, port);
        connect();
    }

    @Override
    protected boolean connectionStatus() {
        serverConnection = connector.connect();
        return serverConnection != null;
    }

    @Override
    protected void discoverMetrics() {
        MBeanInfo mbeaninfo = getMBeanInfo();
        if (mbeaninfo == null) {
            Log.getLogger().severe("No MBean info, failed discovering metrics for module " + moduleName);
            return;
        }
        MBeanAttributeInfo[] attribs = mbeaninfo.getAttributes();
        for (MBeanAttributeInfo attrib : attribs) {
            addMetric(
                    attrib.getName(),
                    MetricDescriptionParser.decodeToProperties(attrib.getDescription()).type(
                            nameToType(attrib.getType())));
        }
    }

    @Override
    protected Object getRemoteGenericMetric(String metric) {
        return getRemoteValueFromMBean(metric);
    }

    @Override
    protected boolean setRemoteValueByName(String metric, String value) {
        return setRemoteValueOnMBean(metric, value);
    }

    @Override
    protected boolean setRemoteValueByName(String metric, long value) {
        return setRemoteValueOnMBean(metric, value);
    }

    @Override
    protected boolean setRemoteValueByName(String metric, boolean value) {
        return setRemoteValueOnMBean(metric, value);
    }

    private TYPE nameToType(String type) {
        if ("java.lang.Long".equals(type)) {
            return TYPE.NUMBER;
        } else if ("java.lang.String".equals(type)) {
            return TYPE.TEXT;
        } else if ("java.lang.Boolean".equals(type)) {
            return TYPE.BOOLEAN;
        }
        return TYPE.INVALID;
    }

    private MBeanInfo getMBeanInfo() {
        try {
            return serverConnection.getMBeanInfo(mbeanName);
        } catch (InstanceNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "Discovering metrics, mbean insatnce not found", e);
        } catch (IntrospectionException e) {
            Log.getLogger().log(Level.SEVERE, "Discovering metrics, introspection error", e);
        } catch (ReflectionException e) {
            Log.getLogger().log(Level.SEVERE, "Discovering metrics, reflection error", e);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "Discovering metrics, IO error", e);
        }
        return null;
    }

    private ObjectName getMBeanObjectName() {
        ObjectName objectName = null;
        try {
            objectName = new ObjectName(jmxResolver.moduleJMXName(moduleName));
        } catch (MalformedObjectNameException e) {
            Log.getLogger().log(Level.SEVERE, "Malformed error", e);
        } catch (NullPointerException e) {
            Log.getLogger().log(Level.SEVERE, "Null error", e);
        }
        return objectName;
    }

    private Object getRemoteValueFromMBean(String metric) {
        try {
            return serverConnection.getAttribute(mbeanName, metric);
        } catch (AttributeNotFoundException e) {
            handleException(metric, e);
        } catch (InstanceNotFoundException e) {
            handleException(metric, e);
        } catch (MBeanException e) {
            handleException(metric, e);
        } catch (ReflectionException e) {
            handleException(metric, e);
        } catch (IOException e) {
            Log.getLogger().warning("Connection failed for " + moduleName + ", " + e.getMessage() + ", Retrying ...");
            reconnect(); // retry failed connection
        }
        Log.getLogger().warning("Failed getting metric from " + moduleName);
        return null;
    }

    private void handleException(String metric, Exception exception) {
        Log.getLogger().log(Level.SEVERE, "Failed getting" + metric + " from " + moduleName, exception);
    }

    private boolean setRemoteValueOnMBean(String metric, Object value) {
        try {
            serverConnection.setAttribute(mbeanName, new Attribute(metric, value));
            return true;
        } catch (InstanceNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "Not found error", e);
        } catch (AttributeNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "No Attribute error", e);
        } catch (InvalidAttributeValueException e) {
            Log.getLogger().log(Level.SEVERE, "Invalid Attribute error", e);
        } catch (MBeanException e) {
            Log.getLogger().log(Level.SEVERE, "Mbean error", e);
        } catch (ReflectionException e) {
            Log.getLogger().log(Level.SEVERE, "Reflection error", e);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "IO error", e);
            Log.getLogger().warning(
                    "Connection failed for " + metric + " on " + moduleName + ", " + e.getMessage() + ". Retrying ...");
            reconnect(); // retry failed connection
        }
        Log.getLogger().warning("Failed to set remote jmx metric " + metric + " = " + value + " on " + moduleName);
        return false;
    }

}
