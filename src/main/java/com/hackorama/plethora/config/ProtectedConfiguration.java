package com.hackorama.plethora.config;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hackorama.plethora.common.Configuration;
import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.io.CheckedFileWriter;
import com.hackorama.plethora.server.io.CopyingFileWriter;

/**
 * Protects selected properties, based on recommendations from OWASP
 * https://www.owasp.org/index.php/How_to_encrypt_a_properties_file
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class ProtectedConfiguration extends Configuration {

    private Protector protector;
    private String protectedPropMarker = "protected.";
    private String keyFilePropertyName;
    private File keyFile;
    private CheckedFileWriter fileWriter;

    public ProtectedConfiguration(String propertyFile) {
        super(propertyFile);
        fileWriter = new CopyingFileWriter();
    }

    public ProtectedConfiguration(String propertyFile, Protector protector) throws Exception {
        this(propertyFile);
        protectValues(protector);
    }

    public ProtectedConfiguration(String propertyFile, Protector protector, CheckedFileWriter fileWriter) {
        this(propertyFile);
        this.fileWriter = fileWriter;
        protectValues(protector);
    }

    public ProtectedConfiguration(String propertyFile, Protector protector, CheckedFileWriter fileWriter,
            String keyFilePropertyName, String protectedPropMarker) throws SecurityException {
        this(propertyFile);
        this.fileWriter = fileWriter;
        this.keyFilePropertyName = keyFilePropertyName;
        this.protectedPropMarker = protectedPropMarker;
        protectValues(protector);
    }

    public ProtectedConfiguration(String propertyFile, Protector protector, String keyFilePropertyName,
            String protectedPropMarker) throws SecurityException {
        this(propertyFile);
        this.keyFilePropertyName = keyFilePropertyName;
        this.protectedPropMarker = protectedPropMarker;
        protectValues(protector);
    }

    public ProtectedConfiguration(String[] argv) {
        super(argv);
        fileWriter = new CopyingFileWriter();
    }

    public ProtectedConfiguration(String[] argv, Protector protector) {
        this(argv);
        protectValues(protector);
    }

    public ProtectedConfiguration(String[] argv, Protector protector, CheckedFileWriter fileWriter) {
        this(argv);
        this.fileWriter = fileWriter;
        protectValues(protector);
    }

    public ProtectedConfiguration(String[] argv, Protector protector, CheckedFileWriter fileWriter,
            String keyFilePropertyName, String protectedPropMarker) throws SecurityException {
        this(argv);
        this.fileWriter = fileWriter;
        this.keyFilePropertyName = keyFilePropertyName;
        this.protectedPropMarker = protectedPropMarker;
        protectValues(protector);
    }

    public ProtectedConfiguration(String[] argv, Protector protector, String keyFilePropertyName,
            String protectedPropMarker) throws SecurityException {
        this(argv);
        this.keyFilePropertyName = keyFilePropertyName;
        this.protectedPropMarker = protectedPropMarker;
        protectValues(protector);
    }

    private String filterLine(String line, Map<String, String> originalValues, Map<String, String> updatedValues) {
        Pattern pattern;
        String updated;
        StringBuffer result = new StringBuffer();
        for (Entry<String, String> entry : originalValues.entrySet()) {
            // The regex : (^\\s*name\\s*[=: ]\\s*"?)(value)("?\\s*$)
            // Name value pair separated by either of = or : or ' '
            // Both name and value can have zero or more surrounding white
            // spaces. Value can be optionally quoted with ".
            // Grouped into three, we only replace the second segment
            pattern = Pattern.compile("(^\\s*" + entry.getKey() + "\\s*[=: ]\\s*\"?)(" + entry.getValue()
                    + ")(\"?\\s*$)");
            Matcher matcher = pattern.matcher(line);
            updated = updatedValues.get(entry.getKey());
            while (matcher.find()) {
                matcher.appendReplacement(result, matcher.group(1) + updated + matcher.group(3));
            }
            matcher.appendTail(result);
        }
        return result.toString();
    }

    @Override
    public final String getProperty(String name) {
        /* making final so subclasses will not change the protection */
        String value = super.getProperty(name);
        if (isProtectedProperty(name)) {
            return plainValue(value);
        }
        return value;
    }

    @Override
    public final String getProperty(String name, String def) {
        /* making final so subclasses will not change the protection */
        String value = super.getProperty(name, def);
        if (isProtectedProperty(name)) {
            return plainValue(value);
        }
        return value;
    }

    private String getUpdatedProperties(Map<String, String> originalValues, Map<String, String> updatedValues) {
        if (updatedValues.isEmpty()) {
            return null;
        }
        String result = ""; // defaults to empty string
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(propertyFile)),
                    Util.getEncoding()));
            String line;
            StringBuilder filteredText = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                filteredText.append(filterLine(line, originalValues, updatedValues));
                filteredText.append(System.getProperty("line.separator"));
            }
            result = filteredText.toString();
        } catch (FileNotFoundException e) { // TODO security or IO exception
            Log.getLogger().log(Level.SEVERE, "Property file " + propertyFile + " not available", e);
        } catch (IOException e) { // TODO securiyException
            Log.getLogger().log(Level.SEVERE, "Failed updating protected values in " + propertyFile, e);
        } finally {
            Util.close(reader, Log.getLogger());
        }
        return result;
    }

    private boolean initKeyFile() {
        if (keyFilePropertyName == null) {
            Log.getLogger().warning("No key file name property provided");
        } else {
            String fileName = getProperty(keyFilePropertyName);
            if (fileName == null) {
                Log.getLogger().warning("No key file for " + keyFilePropertyName);
            } else {
                File file = new File(fileName);
                if (file.canRead()) {
                    keyFile = file;
                    return true;
                } else {
                    Log.getLogger().severe(fileName + " is not readable");
                }
            }
        }
        return false;
    }

    private boolean isProtectedProperty(String name) {
        return name.startsWith(protectedPropMarker);
    }

    private boolean isProtectedValue(String value) {
        try {
            return protector.isProtected(value);
        } catch (ConfigProtectException e) {
            Log.getLogger().log(Level.SEVERE, "Failed in protect validation, this value will be invalid", e);
        }
        return true; // if we can't validate protect by default
    }

    private String plainValue(String value) {
        try {
            return protector.plainValue(value);
        } catch (ConfigProtectException e) {
            Log.getLogger().severe("Error during decoding, returning null");
        } catch (ConfigProtectKeyException e) {
            Log.getLogger().severe("Invalid decoded value, returning null");
        }
        return null;
    }

    private String protectedValue(String value) throws ConfigProtectException {
        return protector.protectedValue(value);
    }

    private String protectProperty(String name, String value) {
        String protectedValue = null;
        if (isProtectedProperty(value)) {
            return null;
        }
        try {
            protectedValue = protectedValue(value);
        } catch (ConfigProtectException e) {
            Log.getLogger().severe("Failed to protect value for " + name + ", setting  empty value instead");
            protectedValue = "";
        }
        super.setProperty(name, protectedValue);
        return protectedValue;
    }

    private void protectValues(Protector protector) throws SecurityException {
        Map<String, String> originalValues = new HashMap<String, String>();
        Map<String, String> updatedValues = new HashMap<String, String>();

        this.protector = protector;
        if (protector == null) {
            throw new SecurityException("No protector provided for configuration values");
        }
        if (initKeyFile()) {
            try {
                protector.setKey(keyFile);
            } catch (ConfigProtectException e) {
                throw new SecurityException("Failed using  key file " + keyFile.getPath(), e);
            } catch (IOException e) {
                throw new SecurityException("IO error using key file " + keyFile.getPath(), e);
            }
        }
        for (Entry<String, String> entry : super.getValuesStartingWith(protectedPropMarker).entrySet()) {
            String key = protectedPropMarker + entry.getKey();
            String originalValue = entry.getValue();
            if (!isProtectedValue(originalValue)) {
                String updatedValue = protectProperty(key, originalValue);
                if (updatedValue != null) {
                    originalValues.put(key, originalValue);
                    updatedValues.put(key, updatedValue);
                }
            }
        }
        String updated = getUpdatedProperties(originalValues, updatedValues);
        if (updated != null) {
            try {
                writePropertyFile(updated);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void setKeyFilePropertyName(String name) {
        keyFilePropertyName = name;
    }

    @Override
    public final Object setProperty(String name, String value) {
        /* making final so subclasses will not change the protection */
        String setValue = value;
        if (isProtectedProperty(name)) {
            try {
                setValue = protectedValue(value);
            } catch (ConfigProtectException e) {
                Log.getLogger().severe("Failed to protect value for " + name + ", setting  empty value instead");
                setValue = "";
            }
        }
        return super.setProperty(name, setValue);

    }

    protected void setProtectedPropertyMarker(String marker) {
        protectedPropMarker = marker;
    }

    private boolean writePropertyFile(String content) throws IOException {
        return propertyFile != null ? fileWriter.fileWrite(new File(propertyFile), content) : false;
    }
}
