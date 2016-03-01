package com.hackorama.plethora.common;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Reusable common utility functions. Exposed as static methods and holds no state information, application specific
 * logic or references. Depends on no application packages.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class Util {

    @SuppressWarnings("unchecked")
    // final static array of known contents, OK to suppress warnings
    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(Arrays.asList(Boolean.class,
            Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));

    public static boolean invalidEmpty(String name) {
        return !validNonEmpty(name);
    }

    public static boolean invalidIndex(int index) {
        return !validIndex(index);
    }

    public static boolean invalidPort(int port) {
        return !validPort(port);
    }

    public static boolean isPrimitiveType(Object object) {
        if (object == null) {
            return false;
        }
        return WRAPPER_TYPES.contains(object.getClass());
    }

    public static boolean validIndex(int index) { // zero indexed
        return index >= 0 ? true : false;
    }

    public static boolean validNonEmpty(String string) {
        return string != null && string.length() > 0 ? true : false;
    }

    public static boolean validPort(int port) {
        return port < 0 || port > 0xFFFF ? false : true; // 0 - 65535
    }

    public static boolean isReadableFile(String path) {
        if (path != null) {
            File file = new File(path);
            return file.exists() && file.canRead();
        }
        return false;
    }

    public static Number getNumber(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            // return null for invalid input
            return null;
        }
    }

    public static Number getNumber(String value, Number def) {
        Number number = getNumber(value);
        return number == null ? def : number;
    }

    public static Long getLong(String value) {
        Number number = getNumber(value);
        return number != null ? number.longValue() : null;
    }

    public static long getLong(String value, long def) {
        Number number = getNumber(value);
        return number != null ? number.longValue() : def;
    }

    public static Integer getInt(String value) {
        Number number = getNumber(value);
        return number != null ? number.intValue() : null;
    }

    public static int getInt(String value, int def) {
        Number number = getNumber(value);
        return number != null ? number.intValue() : def;
    }

    public static boolean typeIsString(String type) {
        return "java.lang.String".equals(type) || "java.lang.Character".equals(type);
    }

    public static boolean typeIsNumber(String type) {
        return "java.lang.Short".equals(type) || "java.lang.Short".equals(type) || "java.lang.Integer".equals(type)
                || "java.lang.Long".equals(type) || "java.lang.Double".equals(type) || "java.lang.Float".equals(type);
    }

    public static boolean typeIsBoolean(String type) {
        return "java.lang.Boolean".equals(type);
    }

    public static boolean isNumeric(String textvalue) {
        try {
            Double.parseDouble(textvalue);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isBoolean(String textvalue) {
        return "true".equalsIgnoreCase(textvalue) || "false".equalsIgnoreCase(textvalue);
    }

    public static String trimWhiteSpace(String value) {
        return value == null ? null : value.trim();
    }

    public static String trimConfigStrings(String value) {
        /* trim whitespace, single and double quotes, tabs */
        return value == null ? null : value.trim().replaceAll("^\"|^\'|^\t|\"$|\'$|\t$", "").trim();
    }

    public static String getEnv(String name, String def) {
        String value = System.getenv(name);
        return value == null ? def : value;
    }

    public static void close(Closeable closable) {
        Util.close(closable, null);
    }

    public static void close(Closeable closable, Logger logger) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                if (logger != null) {
                    logger.warning("Error closing resource, " + e.getMessage());
                }
            }
        }
    }

    public static String getEnvPropValue(String envName, String propName, String def) {
        /*
         * first check system env value, then check if overridden by system property value. Return default value if not
         * defined by either system env or system prop. Return system env value if system prop is not defined
         */
        String envValue = System.getenv(envName);
        envValue = envValue == null ? def : envValue;
        return System.getProperty(propName, envValue);
    }

    public static String getEncoding() {
        // http://www.oracle.com/technetwork/java/javase/tech/faq-jsp-138165.html
        return "UTF-8";
    }

    private Util() {
        // no instances
        throw new AssertionError();
    }

}
