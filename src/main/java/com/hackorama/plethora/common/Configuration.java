package com.hackorama.plethora.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

/**
 * A wrapper around java.util.properties with some helper routines to get similar types of property names identified
 * common prefix or postfix
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class Configuration {
    protected Properties properties;
    protected String propertyFile;

    public Configuration(String propertyFile) {
        properties = getPropertiesFromFile(propertyFile);
    }

    public Configuration(String[] argv) {
        if (argv.length > 0) {
            properties = getPropertiesFromFile(argv[0]);
        } else {
            Log.getLogger().severe("No configuration property file provided to process");
        }
    }

    /**
     * 
     * @param propertyFile
     *            the file to load
     * @return Properties object loaded from the file or null for invalid file or any IO issues
     */
    private final Properties getPropertiesFromFile(String propertyFile) {
        if (!Util.isReadableFile(propertyFile)) {
            Log.getLogger().severe("Skipping invalid/unavailable property file : " + propertyFile);
            return null;
        }
        this.propertyFile = propertyFile; // only if readable file
        Log.getLogger().finest("Reading configuration property file : " + propertyFile);
        Properties props = new Properties();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(propertyFile);
            props.load(stream);
            return props;
        } catch (FileNotFoundException e) {
            Log.getLogger().log(Level.SEVERE, "File not found for " + propertyFile, e);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "IO error on reading " + propertyFile, e);
        } finally {
            Util.close(stream, Log.getLogger());
        }
        return null;
    }

    /**
     * Get all name, values pairs for names starting with the given prefix
     * 
     * @param prefix
     *            All names that starts with this prefix string
     * @return A map of the names and values for names starting with prefix The returned names will be without the
     *         common prefix. Empty map if there was no match
     */
    public Map<String, String> getValuesStartingWith(String prefix) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (properties != null && prefix != null) {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                if (name.startsWith(prefix)) {
                    result.put(name.substring(prefix.length()), (String) entry.getValue());
                }
            }
        }
        return result;
    }

    /**
     * Get all name, values pairs for names ending with the given postfix
     * 
     * @param postfix
     *            All names that ending with this postfix string
     * @return A map of the names and values for names ending with postfix The returned names will be without the common
     *         postfix. Empty map if there was no match
     */
    public Map<String, String> getValuesEndingWith(String postfix) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (properties != null && postfix != null) {
            for (Object key : properties.keySet()) {
                String name = (String) key;
                if (name.endsWith(postfix)) {
                    result.put(name.substring(0, name.length() - postfix.length()), getProperty(name));
                }
            }
        }
        return result;
    }

    /**
     * Get all name, values pairs for names ending with the given prefix
     * 
     * @param prefix
     *            All names that ending with this prefix string
     * @return A map of the names and values for names ending with prefix The returned names will be without the common
     *         prefix. Empty map if there was no match
     */
    public final Map<String, String> getValuesStartingAndEndingWith(String start, String end) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (properties != null && start != null && end != null) {
            for (Object key : properties.keySet()) {
                String name = (String) key;
                if (name.startsWith(start) && name.endsWith(end)) {
                    result.put(name.substring(start.length(), name.length() - end.length()), getProperty(name));
                }
            }
        }
        return result;
    }

    public String getProperty(String name) {
        return Util.trimConfigStrings(getPropertyValue(name));
    }

    public String getProperty(String name, String def) {
        return Util.trimConfigStrings(getPropertyValue(name, def));
    }

    private String getPropertyValue(String name, String def) {
        return properties != null ? properties.getProperty(name, def) : def;
    }

    private String getPropertyValue(String name) {
        return properties != null ? properties.getProperty(name) : null;
    }

    public Object setProperty(String name, String value) {
        return properties == null ? null : properties.setProperty(name, value);
    }

    public Properties getProperties() {
        return properties;
    }

}
