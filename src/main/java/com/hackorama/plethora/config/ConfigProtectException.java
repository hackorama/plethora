package com.hackorama.plethora.config;

import java.util.logging.Level;

import com.hackorama.plethora.common.Log;

/**
 * Logs custom error message and stack trace to the application logs
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class ConfigProtectException extends Exception {
    private static final String ERROR = "Error in protected property access during ";

    public ConfigProtectException(String message, Throwable cause) {
        super(ERROR + message, cause);
        Log.getLogger().log(Level.SEVERE, ERROR + message, cause);
    }
}
