package com.hackorama.plethora.server.annotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Formats PropertyDoc annotations to default property file
 * 
 * Given an annotation like:
 * 
 * <pre>
 * @PropertyDoc(name = "data.refresh.seconds", defaultValue = "5", doc = "How often metric data is collected from the application modules, in seconds", required = true)
 * </pre>
 * 
 * Generates property file as:
 * 
 * <pre>
 * ## data.refresh.seconds : How often metric data is collected from the application modules, in seconds
 * ## This is a required property with default value 5
 * data.refresh.seconds = 5
 * </pre>
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class PropertyHelpDocFormatter implements DocFormatter {

    private static final String EOL = System.getProperty("line.separator");
    private static final String HTML_EOL = "</br>";
    private static final String HEADER = "<html>" + EOL + "<head>" + EOL
            + "  <title>Plethora Configuration Properties</title>" + EOL + "</head>" + EOL + "<body>" + EOL
            + "<h1>Plethora Configuration Properties</h1>" + EOL
            + "<p>These are defined in the configuration file provided as an "
            + "argument to Plethora startup and follows the well known simple "
            + "<a href=http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html#load(java.io.Reader)>"
            + "java properties file format</a>" + EOL + "<p>" + EOL;
    private static final String FOOTER = "</body>" + EOL + "</html>";

    @Override
    public void format(List<? extends Annotation> annotations, BufferedWriter writer) throws IOException {
        writer.write(HEADER);
        writer.newLine();
        writer.write("<table border=\"1\">");
        writer.newLine();
        writer.write("<tr><th>Name</th><th>Default Value</th><th>Type</th><th>Description</th></tr>");
        for (Annotation annotation : annotations) {
            PropertyDoc propertyDoc = (PropertyDoc) annotation;
            if (propertyDoc.name().length() < 1) {
                continue; // skip, invalid annotation
            }
            writer.write("  <tr>");
            writer.newLine();
            writer.write("    <td>" + propertyDoc.name() + "</td>");
            writer.newLine();
            writer.write("    <td>" + propertyDoc.defaultValue() + "</td>");
            writer.newLine();
            String msg = "Optional";
            if (propertyDoc.required()) {
                msg = "Required";
            }
            writer.write("    <td>" + msg + "</td>");
            msg = "";
            if (propertyDoc.doc().length() > 0) {
                // replace any doc string end of lines with HTML breaks
                msg = propertyDoc.doc();
                msg = msg.replaceAll("(\r\n|\n\r|\r|\n)", HTML_EOL);
            }
            writer.write("    <td>" + msg + "</td>");
            writer.newLine();
            writer.write("  </tr>");
            writer.newLine();
        }
        writer.write("</table>");
        writer.newLine();
        writer.write(FOOTER);
    }

}
