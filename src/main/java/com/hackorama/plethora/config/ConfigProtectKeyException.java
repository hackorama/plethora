package com.hackorama.plethora.config;

import java.util.logging.Level;

import com.hackorama.plethora.common.Log;

/**
 * Logs error message to the application logs
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class ConfigProtectKeyException extends Exception {
    private static final String ERROR = "Could not unlock the protected value correctly, check the keys";

    public ConfigProtectKeyException() {
        super(ERROR);
        Log.getLogger().log(Level.SEVERE, ERROR);
    }
}
