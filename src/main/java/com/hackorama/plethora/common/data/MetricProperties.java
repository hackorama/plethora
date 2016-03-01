package com.hackorama.plethora.common.data;

import com.hackorama.plethora.common.METRIC;

/**
 * Metric properties chained builder for selective initialization. Any invalid null input for the string properties will
 * be set to the default empty string.
 * 
 * @author kishan.thomas@gmail.com
 * 
 */
public class MetricProperties {
    // uses matching field and method names as a builder
    private String displayname;
    private String description;
    private METRIC.LEVEL level;
    private METRIC.TYPE type; // initial provided type not actual type
    private boolean readable;
    private boolean writable;

    public MetricProperties() {
        displayname = "";
        description = "";
        level = METRIC.LEVEL.defaultLevel();
        type = METRIC.TYPE.defaultType();
        readable = true;
        writable = false;
    }

    public MetricProperties displayname(String displayname) {
        if (displayname != null) { // do not allow null
            this.displayname = displayname;
        }
        return this;
    }

    public MetricProperties description(String description) {
        if (description != null) { // do not allow null
            this.description = description;
        }
        return this;
    }

    public MetricProperties level(METRIC.LEVEL level) {
        this.level = level;
        return this;
    }

    public MetricProperties type(METRIC.TYPE type) {
        this.type = type;
        return this;
    }

    public MetricProperties readable() {
        readable = true;
        return this;
    }

    public MetricProperties readable(boolean value) {
        readable = value;
        return this;
    }

    public MetricProperties writable() {
        writable = true;
        return this;
    }

    public MetricProperties writable(boolean value) {
        writable = value;
        return this;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getDescription() {
        return description;
    }

    public METRIC.LEVEL getLevel() {
        return level;
    }

    public METRIC.TYPE getType() {
        return type;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }
}
