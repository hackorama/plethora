package com.hackorama.plethora.server.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * Example of a web request handler being annotated with this annotation
 * 
 * <pre>
 * {@literal @}WebApiDoc(uri = "/get/{metric}", doc = "Returns the value for the metric")
 * public class MetricRequestHandler extends DataRequestHandler {
 *    ...
 * }
 * </pre>
 * 
 * The annotation will be used by the annotation processor to self document the web API by generating any required
 * help/doc files.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * @see
 * 
 */
@Retention(RetentionPolicy.SOURCE)
public @interface WebApiDoc {
    String uri() default "";

    String action() default "GET";

    String doc() default "";
}
