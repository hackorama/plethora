package com.hackorama.plethora.channel;

import java.util.logging.Level;

import com.hackorama.plethora.common.Configuration;
import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.common.jmx.PlethoraMBean;
import com.hackorama.plethora.common.jmx.JMXAgent;

public class Channel {

    private static final String SYS_PROP_PREFIX = "plethora.";
    private static final String PROP_HOST = SYS_PROP_PREFIX + "host";
    private static final String PROP_PORT = SYS_PROP_PREFIX + "port";

    private final String module;
    private JMXAgent agent = null;
    private final Metrics metrics;
    private final ChannelMetrics channelMetrics;
    private PlethoraMBean mbean = null;
    private String host = null;
    private int port = 0;

    public Channel(String name, String configfile, java.util.logging.Logger logger) {
        Log.setLogger(logger);
        module = name;
        metrics = Metrics.getInstance();
        channelMetrics = new ChannelMetrics(metrics);
        initMbean(configfile);
    }

    private final void initMbean(String configfile) {
        if (readConfigurationOptions(configfile)) {
            mbean = new PlethoraMBean(new PropertiesMetricsReader(metrics).getMetricsFromFile(configfile,
                    SYS_PROP_PREFIX));
        }
    }

    public boolean launch() {
        if (agent == null) {
            threadSafeLaunch();
        }
        if (agent != null && agent.isReady()) {
            Log.getLogger().info("Started channel jmx agent " + agent.getMbeanName() + " at " + host + ":" + port);
            Log.getLogger().info("Started plethora channel " + module);
            return true;
        } else {
            Log.getLogger().severe("Plethora channel not available for " + module);
            return false;
        }
    }

    public Object getMetric(String name) {
        return metrics == null ? null : metrics.getValue(name);
    }

    public Long getNumberMetric(String name) {
        return channelMetrics.getNumberValue(name);
    }

    public String getTextMetric(String name) {
        return channelMetrics.getTextValue(name);
    }

    public Boolean getBooleanMetric(String name) {
        return channelMetrics.getBooleanValue(name);
    }

    public boolean incrMetric(String name) {
        return incrMetric(name, 1);
    }

    public boolean incrMetric(String name, long delta) {
        Long currentvalue = getNumberMetric(name);
        if (currentvalue == null) {
            return false;
        }
        return setMetric(name, currentvalue.longValue() + delta);
    }

    public boolean decrMetric(String name) {
        return incrMetric(name, 1);
    }

    public boolean decrMetric(String name, long delta) {
        Long currentvalue = getNumberMetric(name);
        if (currentvalue == null) {
            return false;
        }
        return setMetric(name, currentvalue.longValue() - delta);
    }

    public boolean setMetric(String name, long value) {
        return metrics == null ? false : metrics.setValue(name, value);
    }

    public boolean setMetric(String name, String value) {
        return metrics == null ? false : metrics.setValue(name, value);
    }

    public boolean setMetric(String name, boolean value) {
        return metrics == null ? false : metrics.setValue(name, value);
    }

    private boolean readConfigurationOptions(String configfile) {
        if (!Util.isReadableFile(configfile)) {
            Log.getLogger().warning("Invalid/unavailable property file : " + configfile);
            return false;
        }
        Configuration configuration = new Configuration(configfile);
        this.host = configuration.getProperty(PROP_HOST);
        this.port = getPortNumber(configuration);
        return true;
    }

    /*
     * Thread safe single agent launch
     * 
     * NOTE: Synchronization used only during initialization, so no block level synchronization optimization
     */
    private synchronized boolean threadSafeLaunch() {
        if (agent == null) {
            try {
                agent = new ClientJMXAgent(module, mbean, host, port);
                agent.start();
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Agent failed " + module, e);
                return false;
            }
        }
        return true;
    }

    private int getPortNumber(Configuration configuration) {
        return Util.getInt(configuration.getProperty(PROP_PORT), 0);
    }
}