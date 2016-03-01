package com.hackorama.plethora.common;

/**
 * The descriptions used for the metrics properties when they will be encoded for remote access between plethora server
 * and client.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class MetricPropertyDescription {

    // METRIC.ATTRIBUTE.NAME MetricProperties.displayname
    protected static final String DISPLAY_NAME = "name";
    // METRIC.ATTRIBUTE.DESCRIPTION MetricProperties.description
    protected static final String DESCRIPTION = "description";
    // Char map of METRIC.ATTRIBUTE.TYPE METRIC.ATTRIBUTE.LEVEL and Write Access
    // MetricProperties.type MetricProperties.level MetricProperties.writable
    protected static final String OPTIONS = "options";

}
