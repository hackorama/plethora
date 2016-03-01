package com.hackorama.plethora.channel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.METRIC.ATTRIBUTE;
import com.hackorama.plethora.common.METRIC.LEVEL;
import com.hackorama.plethora.common.METRIC.TYPE;
import com.hackorama.plethora.common.data.MetricFactory;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;

public class PropertiesMetricsReader implements MetricsReader {

    private static final String CONFIG_KEY_SEPARATOR = ".";
    private final Metrics metrics;

    public PropertiesMetricsReader(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public Metrics getMetricsFromFile(String propertyfile) {
        return getMetricsFromFile(propertyfile, null);
    }

    @Override
    public Metrics getMetricsFromFile(String propertyfile, String sysPropertyPrefix) {
        processMetricsConfig(propertyfile, sysPropertyPrefix);
        return metrics;
    }

    private void processMetricsConfig(String propertyFile, String sysPropertyPrefix) {
        if (!Util.isReadableFile(propertyFile)) {
            Log.getLogger().warning("Skipping invalid/unavailable property file : " + propertyFile);
            return;
        }
        Log.getLogger().finest("Reading property file : " + propertyFile);
        Properties props = new Properties();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(propertyFile);
            props.load(stream);
        } catch (FileNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "File not found for " + propertyFile, e);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "IO error on reading " + propertyFile, e);
        } finally {
            Util.close(stream, Log.getLogger());
        }
        for (Object prop : props.keySet()) {
            String key = (String) prop;
            if (isMetricKey(key, sysPropertyPrefix)) {
                String metric = getMetricNameFromPropertyKey(key);
                if (!existingMetric(metric)) {
                    String value = props.getProperty(metric);
                    TYPE type = getType(getAttribute(ATTRIBUTE.TYPE, props, metric, TYPE.UNSPECIFIED.toString()));
                    addMetric(metric, value, type, getMetricProperties(metric, props));
                }
            }
        }
    }

    private boolean isMetricKey(String key, String sysPropertyPrefix) {
        if (Util.invalidEmpty(sysPropertyPrefix)) {
            return true;
        }
        return !key.startsWith(sysPropertyPrefix);
    }

    private MetricProperties getMetricProperties(String metric, Properties props) {
        MetricProperties metricprops = new MetricProperties();
        String name = getAttribute(ATTRIBUTE.NAME, props, metric, metric);
        String description = getAttribute(ATTRIBUTE.DESCRIPTION, props, metric, "");
        LEVEL level = getLevel(getAttribute(ATTRIBUTE.LEVEL, props, metric));
        return metricprops.displayname(name).description(description).level(level);
    }

    private String getAttribute(ATTRIBUTE attribute, Properties props, String metric) {
        String result = props.getProperty(metric + CONFIG_KEY_SEPARATOR + attribute.name().toLowerCase());
        if (result == null) {
            result = props.getProperty(metric + CONFIG_KEY_SEPARATOR + attribute.name().toUpperCase());
        }
        return result == null ? result : Util.trimConfigStrings(result);
    }

    private String getAttribute(ATTRIBUTE attribute, Properties props, String metric, String defaultvalue) {
        String result = getAttribute(attribute, props, metric);
        return result == null ? defaultvalue : result;
    }

    private void addMetric(String name, String textvalue, TYPE type, MetricProperties properties) {
        TYPE thetype = type;
        if (thetype == TYPE.UNSPECIFIED) {
            thetype = valueType(textvalue);
            Log.getLogger().info(name + " : No metric type specified, using " + thetype + " based on initial value");
        } else if (thetype == TYPE.INVALID) {
            thetype = valueType(textvalue);
            Log.getLogger().info(
                    name + " : Invalid metric type provided, changing to " + thetype + " based on initial value");
        }
        properties.type(thetype); // update the type
        metrics.addMetric(MetricFactory.getMetric(name, textvalue, properties));
    }

    private TYPE valueType(String textvalue) {
        if (Util.isNumeric(textvalue)) {
            return TYPE.NUMBER;
        } else if (Util.isBoolean(textvalue)) {
            return TYPE.BOOLEAN;
        }
        return TYPE.TEXT;
    }

    private LEVEL getLevel(String thelevel) {
        for (LEVEL level : LEVEL.values()) {
            if (level.name().equalsIgnoreCase(thelevel)) {
                return level;
            }
        }
        return METRIC.LEVEL.defaultLevel();
    }

    private TYPE getType(String thetype) {
        for (TYPE type : TYPE.values()) {
            if (type.name().equalsIgnoreCase(thetype)) {
                return type;
            }
        }
        return TYPE.INVALID;
    }

    private boolean existingMetric(String metric) {
        return metrics.hasMetric(metric);
    }

    private String getMetricNameFromPropertyKey(String key) {
        for (ATTRIBUTE attrib : ATTRIBUTE.values()) {
            if (key.endsWith(CONFIG_KEY_SEPARATOR + attrib.name().toLowerCase())) {
                return key.substring(0, key.lastIndexOf(CONFIG_KEY_SEPARATOR));
            }
            if (key.endsWith(CONFIG_KEY_SEPARATOR + attrib.name().toUpperCase())) {
                return key.substring(0, key.lastIndexOf(CONFIG_KEY_SEPARATOR));
            }
        }
        return key;
    }
}
