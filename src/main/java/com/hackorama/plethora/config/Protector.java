package com.hackorama.plethora.config;

import java.io.File;
import java.io.IOException;

/**
 * Protects plain text configuration values by encoding with keys
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public interface Protector {

    String protectedValue(String value) throws ConfigProtectException;

    String plainValue(String value) throws ConfigProtectException, ConfigProtectKeyException;

    boolean isProtected(String value) throws ConfigProtectException;

    boolean setKey(File file) throws ConfigProtectException, IOException;

    void setKey(String key);

}
