package com.hackorama.plethora.server.annotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Formats WebApiDoc annotations to documentation/help files
 * 
 * Given an annotation like:
 * 
 * <pre>
 * @WebApiDoc(uri = "/get/{metric}", doc = "Returns the value for the metric")
 * </pre>
 * 
 * Generates HTML documentation as
 * 
 * <pre>
 * <table>
 * <tr><th>Action</th><th>URI</th><th>Description</th></tr>
 * <tr><td>GET</td><td></get/{metric}</td><td>Returns the value for the metric</td></tr>
 * </table>
 * </pre>
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class WebApiHelpDocFormatter implements DocFormatter {

    private static final String EOL = System.getProperty("line.separator");
    private static final String HEADER = "<html>" + EOL + "<head>" + EOL
            + "  <title>Plethora Web API Documentation</title>" + EOL + "</head>" + EOL + "<body>" + EOL
            + "<h1>Plethora Web API Documentation</h1>";
    private static final String FOOTER = "</body>" + EOL + "</html>";

    @Override
    public void format(List<? extends Annotation> annotations, BufferedWriter writer) throws IOException {
        writer.write(HEADER);
        writer.newLine();
        writer.write("<table border=\"1\">");
        writer.newLine();
        writer.write("<tr><th>Action</th><th>URI</th><th>Description</th></tr>");
        writer.newLine();
        for (Annotation annotation : annotations) {
            WebApiDoc webApiDoc = (WebApiDoc) annotation;
            if (webApiDoc.uri().length() < 1) {
                continue; // skip, invalid annotation
            }
            writer.write("<tr>");
            writer.write("<td>" + webApiDoc.action() + "</td>");
            writer.write("<td>" + webApiDoc.uri() + "</td>");
            writer.write("<td>" + webApiDoc.doc() + "</td>");
            writer.write("</tr>");
            writer.newLine();
        }
        writer.write("</table>");
        writer.newLine();
        writer.write(FOOTER);
    }

}
