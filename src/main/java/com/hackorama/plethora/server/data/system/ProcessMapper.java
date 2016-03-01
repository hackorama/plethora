package com.hackorama.plethora.server.data.system;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide process name and pid from a mapping file with implementation specific format like java properties file, json,
 * xml etc.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public interface ProcessMapper {
    /**
     * Provide process name and pid map from the given mapping file.
     * 
     * @param mappingFile
     *            the configuration file
     * @return
     */
    ConcurrentHashMap<String, Long> getMap(String mappingFile);

    /**
     * Provide process name and pid map from the default mapping file.
     * 
     * @return
     */
    ConcurrentHashMap<String, Long> getMap();
}
