package com.hackorama.plethora.server.data.system;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

/**
 * Reads a java property file with process name and pid mapping. Pid values provided as either the actual pid numeric or
 * a string pointing to a valid file path that contains the numeric pid or a pid query string used by a system access
 * library like SIGAR (http://support.hyperic.com/display/SIGAR/PTQL)
 * 
 * database = 1942 appserver1 = /etc/app/server1.pid appserver2 = /etc/app/server2.pid dynamichandler =
 * "State.Name.eq=java,Args.-1.eq=com.dyn.handler"
 * 
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class PropertiesProcessMapper implements ProcessMapper {

    // allow safe access from multiple threads
    ConcurrentHashMap<String, Long> pidMap;
    private final SystemAccess sysAccess;
    String propertyFile;

    PropertiesProcessMapper(String propertyFile) {
        this(propertyFile, null);
    }

    public PropertiesProcessMapper(String propertyFile, SystemAccess sysAccess) {
        this.propertyFile = propertyFile;
        this.sysAccess = sysAccess != null ? sysAccess : new SigarFacade();
        pidMap = new ConcurrentHashMap<String, Long>();
    }

    @Override
    public ConcurrentHashMap<String, Long> getMap(String layoutFile) {
        this.propertyFile = layoutFile;
        return processConfig();
    }

    @Override
    public ConcurrentHashMap<String, Long> getMap() {
        return getMap(propertyFile);
    }

    private ConcurrentHashMap<String, Long> processConfig() {
        if (propertyFile != null) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(propertyFile);
                Properties properties = new Properties();
                properties.load(stream);
                makePidMap(properties);
            } catch (FileNotFoundException e) {
                Log.getLogger().log(Level.WARNING, "Process layout fle is not availble " + propertyFile, e);
            } catch (IOException e) {
                Log.getLogger().log(Level.WARNING, "Error reading process layout file " + propertyFile, e);
            } finally {
                Util.close(stream, Log.getLogger());
            }
        }
        return pidMap;
    }

    private void makePidMap(Properties properties) {
        pidMap = new ConcurrentHashMap<String, Long>();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            String name = (String) entry.getKey();
            String value = Util.trimConfigStrings((String) entry.getValue());
            pidMap.put(name, resolvePid(value));
        }
    }

    private long resolvePid(String value) {
        long pid = 0;
        // 1. pid provided
        pid = getPid(value);
        if (pid == 0) {
            // using a pid file
            pid = getPid(getFileContent(value));
        }
        if (pid == 0) {
            // using systemAccess implementation based pid query string
            pid = findProcess(value);
        }
        return pid > 0 && sysAccess.isAvaialbleProcess(pid) ? pid : 0;
    }

    private long findProcess(String queryString) {
        if (queryString != null) {
            try {
                return sysAccess.findPid(queryString);
            } catch (SystemAccessException e) {
                String msg = "Failed getting pid for " + queryString;
                Log.getLogger().warning(msg + " : " + e.getMessage());
                Log.getLogger().log(Level.FINEST, msg, e);
            }
        }
        return 0;
    }

    private long getPid(String value) {
        return Util.getLong(value, 0);
    }

    private boolean isValidFile(String name) {
        return Util.isReadableFile(name);
    }

    private String getFileContent(String name) {
        if (isValidFile(name)) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(name), Util.getEncoding()));
                // expecting single line files
                return Util.trimConfigStrings(reader.readLine());
            } catch (IOException e) {
                Log.getLogger().warning("Error reading pid file " + name);
            } finally {
                Util.close(reader, Log.getLogger());
            }
        }
        return null;
    }
}
