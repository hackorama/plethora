package com.hackorama.plethora.server.io;

/**
 * Central controller for resource access permissions. Handles file access permissions. Other resource permissions can
 * be added later here.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public enum SecurityManager {
    INSTANCE;

    // TODO Use java.lang.SecurityManager if we need more extensive checks
    // including any file system access from untrusted third party libraries.

    private static final FileAccessPermission FILE_ACCESS_PERM = new FileAccessPermission();

    public static SecurityManager getInstance() {
        return INSTANCE;
    }

    private SecurityManager() {
    }

    public FileAccessPermission getFileAccessPermission() {
        return FILE_ACCESS_PERM;
    }
}
