package com.hackorama.plethora.server.annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * This annotation processor will process the different source level annotations and generate any required configuration
 * files and self documentation of any help files.
 * 
 * 1. Build the annotation processor and annotation classes
 * 
 * <pre>
 * ls com/hackorama/plethora/server/annotations/
 * DocAnnotationProcessor.java
 * PropertyDoc.java
 * WebApiDoc.java
 * {AnyOtherAnnotation}.java
 * 
 * <pre>
 * 
 * <pre>
 * javac -cp com/hackorama/plethora/server/annotations/  com/hackorama/plethora/server/annotations/*.java
 * </pre>
 * 
 * 2. Package the annotation processor as jar with the full class name of the annotation processor in the annotation
 * processing service provider
 * 
 * <pre>
 * cat META-INF/services/javax.annotation.processing.Processor
 * com.hackorama.plethora.server.annotations.DocAnnotationProcessor
 * </pre>
 * 
 * <pre>
 * jar -cvfe DocAnnotationProcessor.jar  META-INF/ com/hackorama/plethora/server/annotations/*.class
 * </pre>
 * 
 * 3. Process source files with the annotation processor
 * 
 * <pre>
 * javac   -Aprop.file=plethora.conf -cp DocAnnotationProcessor.jar SourceFileToProcessForAnnotation.java
 * </pre>
 * 
 * Add <code>DocAnnotationProcessor.jar</code> into the <code>Makefile/ant</code> build process and IDE build process.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
@SupportedOptions(value = { "prop.file", "prop.help.file", "api.help.file" })
@SupportedAnnotationTypes({ "com.hackorama.plethora.server.annotations.PropertyDoc",
        "com.hackorama.plethora.server.annotations.WebApiDoc" })
public class DocAnnotationProcessor extends AbstractProcessor {

    private String propFile = "plethora.conf";
    private String propHelpFile = "conf.html";
    private String apiHelpFile = "api.html";
    private Map<String, String> optionsMap;
    private Messager messager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager = processingEnv.getMessager();
        processOptions();
        try {
            processPropertyAnnotations(annotations, roundEnv);
            processWebApiAnnotations(annotations, roundEnv);
        } catch (IOException e) {
            messager.printMessage(Kind.ERROR, "IO error during annotation procesing");
            e.printStackTrace();
        }
        return true;
    }

    private void processOptions() {
        optionsMap = processingEnv.getOptions();
        propFile = getOption("prop.file", propFile);
        propHelpFile = getOption("prop.help.file", propHelpFile);
        apiHelpFile = getOption("api.help.file", apiHelpFile);
    }

    private String getOption(String name, String def) {
        String option = optionsMap.get(name);
        if (option != null && option.length() > 0) {
            return option;
        }
        return def;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void processPropertyAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
            throws IOException {
        List<PropertyDoc> propertyDocs = new ArrayList<PropertyDoc>();
        for (Element element : roundEnv.getElementsAnnotatedWith(PropertyDoc.class)) {
            PropertyDoc propDoc = element.getAnnotation(PropertyDoc.class);
            if (propDoc != null) {
                propertyDocs.add(propDoc);
            }
        }
        if (propertyDocs.size() > 0) {
            messager.printMessage(Kind.NOTE, "Writing configuration properties to " + propFile);
            new DocWriter(propFile, new PropertyDocFormatter()).write(propertyDocs);
            messager.printMessage(Kind.NOTE, "Writing configuration help to " + propHelpFile);
            new DocWriter(propHelpFile, new PropertyHelpDocFormatter()).write(propertyDocs);
        }
    }

    private void processWebApiAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
            throws IOException {
        List<WebApiDoc> webApiDocs = new ArrayList<WebApiDoc>();
        for (Element element : roundEnv.getElementsAnnotatedWith(WebApiDoc.class)) {
            WebApiDoc webApiDoc = element.getAnnotation(WebApiDoc.class);
            if (webApiDoc != null) {
                webApiDocs.add(webApiDoc);
            }
        }
        if (webApiDocs.size() > 0) {
            messager.printMessage(Kind.NOTE, "Writing API help to " + apiHelpFile);
            new DocWriter(apiHelpFile, new WebApiHelpDocFormatter()).write(webApiDocs);
        }
    }

}
