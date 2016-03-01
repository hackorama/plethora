package com.hackorama.plethora.server.annotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
public class PropertyDocFormatter implements DocFormatter {

    private static final String COMMENT_TAG = "# ";
    private static final String DOC_COMMENT_TAG = "## ";

    @Override
    public void format(List<? extends Annotation> annotations, BufferedWriter writer) throws IOException {
        for (Annotation annotation : sortedList(annotations)) {
            PropertyDoc propertyDoc = (PropertyDoc) annotation;
            if (propertyDoc.name().length() < 1) {
                continue; // skip, invalid annotation
            }
            String msg = "";
            writer.write(DOC_COMMENT_TAG + propertyDoc.name());
            if (propertyDoc.doc().length() > 0) {
                // prefix doc comment tag to all doc string end of lines
                msg = " : " + propertyDoc.doc();
                msg = msg.replaceAll("(\r\n|\n\r|\r|\n)", System.getProperty("line.separator") + DOC_COMMENT_TAG);
                writer.write(msg);
            }
            writer.newLine();
            msg = "This is an optional property";
            if (propertyDoc.required()) {
                msg = "This is a required property";
            }
            writer.write(DOC_COMMENT_TAG + msg);
            msg = "with no default value provided";
            if (propertyDoc.defaultValue().length() > 0) {
                msg = "with default value " + propertyDoc.defaultValue();
            }
            writer.write(" " + msg);
            writer.newLine();
            if (!propertyDoc.required()) { // optional property
                writer.write(COMMENT_TAG); // will be commented out
            }
            writer.write(propertyDoc.name() + " = " + propertyDoc.defaultValue());
            writer.newLine();
            writer.newLine();
        }
    }

    /**
     * Given a list of mixed required and other property type annotation, returns a new list sorted by annotation type
     * with required property types on top of the list followed by other types.
     * 
     * @param annotations
     *            List of mixed property annotation types
     * @return New List of property annotations sorted by type, with required property on top of the list
     */
    private List<? extends Annotation> sortedList(List<? extends Annotation> annotations) {
        List<Annotation> sorted = new ArrayList<Annotation>();
        int cursor = 0; // tracks required type last insertion point
        for (Annotation annotation : annotations) {
            PropertyDoc propertyDoc = (PropertyDoc) annotation;
            if (propertyDoc.required()) {
                sorted.add(cursor++, annotation); // move to top
            } else {
                sorted.add(annotation);
            }
        }
        return sorted;
    }

}
