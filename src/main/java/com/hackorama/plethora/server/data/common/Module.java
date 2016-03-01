package com.hackorama.plethora.server.data.common;

import java.util.Set;
import java.util.TreeSet;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.data.MetricFactory;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.common.data.NameResolver;

/**
 * 
 * Base module proxy for JMX, HTTP and other metrics. Initial version supports numbers, booleans and string values. List
 * and Map values can be easily added as required
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public abstract class Module {

    protected String moduleName;
    protected boolean connected = false;
    // NOTE: metricNames collection is concurrently accessed from connection
    // manager thread and metric service main thread and data refresh thread.
    // But only updated by connection manager thread, other threads do not
    // iterate (operates on a safe copy returned by toArray() call) or modify
    // the collection.
    //
    // If this behavior changes, wrap this as synchronizedCollection and
    // synchronize any iterations.
    protected Set<String> metricNames = new TreeSet<String>();
    protected Metrics metrics;
    protected NameResolver resolver;

    protected Module() {
        metrics = null;
        resolver = null;
    }

    protected Module(Metrics metrics) {
        this();
        this.metrics = metrics;
        resolver = metrics.getResolver();
    }

    protected Module(Metrics metrics, String name) {
        this(metrics);
        this.moduleName = name;
    }

    public String getName() {
        return moduleName;
    }

    public Set<String> getMetricNames() {
        return metricNames;
    }

    public int getMetricCount() {
        return metricNames.size();
    }

    public abstract Object getValue(String metric);

    protected abstract boolean setValue(String metric, String value);

    public abstract boolean setValue(String metric, long value);

    public abstract boolean setValue(String metric, boolean value);

    public abstract boolean setValue(String metric, Object value);

    protected abstract void discoverMetrics();

    protected void addMetric(String metricName, MetricProperties properties) {
        metricNames.add(metricName);
        String fullMetricName = resolver.resolveMetricName(metricName, moduleName);
        metrics.addMetric(MetricFactory.getMetric(fullMetricName, properties));
        Log.getLogger().info("Added metric " + fullMetricName + " as " + properties.getType());
    }

    protected abstract boolean connectionStatus();

    public final boolean isConnected() {
        return connected;
    }

    public boolean pokeModule() {
        return true;
    }

    public final boolean connect() {
        connected = false;
        if (connectionStatus()) {
            discoverMetrics();
            connected = true;
            Log.getLogger().info("Module " + moduleName + " connected");
        }
        return connected;
    }

    protected boolean reconnect() {
        // implement any housekeeping before reconnection here
        return connect();
    }

}
