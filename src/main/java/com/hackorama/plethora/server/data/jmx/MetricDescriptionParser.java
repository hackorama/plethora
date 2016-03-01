package com.hackorama.plethora.server.data.jmx;

import com.hackorama.plethora.common.MetricPropertyDescription;
import com.hackorama.plethora.common.OptionString;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.server.data.common.JSONParser;

/**
 * 
 * Create a MetricProperty object form JSON encoded metric properties description string.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
final class MetricDescriptionParser extends MetricPropertyDescription {

    public static MetricProperties decodeToProperties(String jsonEncodedProperties) {
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.displayname(JSONParser.getJsonValue(jsonEncodedProperties, DISPLAY_NAME));
        metricProperties.description(JSONParser.getJsonValue(jsonEncodedProperties, DESCRIPTION));
        String options = JSONParser.getJsonValue(jsonEncodedProperties, OPTIONS);
        if (options != null) {
            metricProperties.type(OptionString.getType(options));
            metricProperties.level(OptionString.getLevel(options));
            metricProperties.writable(OptionString.getWritable(options));
        }
        return metricProperties;
    }

}
