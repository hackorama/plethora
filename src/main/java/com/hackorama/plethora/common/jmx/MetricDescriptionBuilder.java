package com.hackorama.plethora.common.jmx;

import com.hackorama.plethora.common.MetricPropertyDescription;

/**
 * Create a JSON encoded metric properties description string from a set of Metric properties.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class MetricDescriptionBuilder extends MetricPropertyDescription {

    protected static final char QUOTE = '"';

    /**
     * Create a JSOn encoded string with metric properties, without using a JSON library, so that there is no extra
     * library dependency on the plethora client.
     * 
     * @param displayName
     *            The metric display name property
     * @param description
     *            The metric description property
     * @param options
     *            The rest of the metric property options as a string
     * @return JSON encoded string
     */
    public static String encodeToJSON(String displayName, String description, String options) {
        /*
         * Not using JSON library by design to not have that dependency on the plethora channel client jar. Not
         * validating input, expects valid defaults from MetricProperties
         */
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(buildPair(OPTIONS, options));
        builder.append(',');
        builder.append(buildPair(DISPLAY_NAME, displayName));
        builder.append(',');
        builder.append(buildPair(DESCRIPTION, description));
        builder.append("}");
        return builder.toString();
    }

    private static String buildPair(String name, String value) {
        return new StringBuilder().append(QUOTE).append(name).append(QUOTE).append(':').append(QUOTE).append(value)
                .append(QUOTE).toString();
    }
}
