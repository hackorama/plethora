package com.hackorama.plethora.server.annotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Formats data from annotations
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public interface DocFormatter {

    /**
     * Generate appropriate documentation or configuration files from a list of annotations and writes them using the
     * provided writer object.
     * 
     * @param annotations
     *            A List of annotation objects
     * @throws IOException
     *             If an I/O error occurs
     */
    void format(List<? extends Annotation> annotations, BufferedWriter writer) throws IOException;

}
