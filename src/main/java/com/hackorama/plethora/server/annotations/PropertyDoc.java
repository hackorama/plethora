package com.hackorama.plethora.server.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Example of a property get method being annotated with this annotation
 * 
 * </pre> {@literal @}PropertyDoc(name = "data.refresh.seconds", defaultValue = "5", doc =
 * "Defines the amount foo in kung foo") public long getFooProperty() { ... } </pre>
 * 
 * The annotation processor will parse these annotation and generate a default property file with default values and doc
 * description as comments, it can also self document the properties by generating any required help/doc files.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
@Retention(RetentionPolicy.SOURCE)
public @interface PropertyDoc {
    String name() default "";

    String defaultValue() default "";

    String doc() default "";

    boolean required() default false;

}
