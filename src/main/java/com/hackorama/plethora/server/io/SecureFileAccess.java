package com.hackorama.plethora.server.io;

import java.io.File;
import java.io.IOException;

import com.hackorama.plethora.common.Log;

/**
 * Secure file access verification by checking the permissions defined in FileAccessPermission as well as actual system
 * file permissions.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class SecureFileAccess {

    private final FileAccessPermission accessPermission;

    public SecureFileAccess(FileAccessPermission accessPermission) {
        this.accessPermission = accessPermission;
    }

    protected File getTrustedValidReadableFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IOException("Invalid path, requries non null path");
        }
        File file = new File(fileName);
        if (!accessPermission.isReadPermittedPath(file.getCanonicalPath())) {
            Log.getLogger().severe("Security warning, accessing unpermitted path " + fileName);
            throw new SecurityException("Not a permitted white listed path to read from : " + fileName);
        }
        if (file.exists() && file.canRead()) {
            return file;
        }
        throw new IOException("Not a valid read permitted path : " + fileName);
    }

    protected File getTrustedValidReadableFile(String fileName, String verifyParentDir) throws IOException {
        File file = verifyParent(getTrustedValidReadableFile(fileName), verifyParentDir);
        if (file == null) {
            throw new IOException("Not a valid or permitted path : " + fileName);
        }
        return file;
    }

    protected File getTrustedValidWritableFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IOException("Invalid path, requries non null path");
        }
        File file = new File(fileName);
        if (!accessPermission.isWritePermittedPath(file.getCanonicalPath())) {
            Log.getLogger().severe("Security warning, accessing unpermitted path " + fileName);
            throw new IOException("Not a permitted white listed path to write : " + fileName);
        }
        if (file.exists() && file.canWrite()) {
            return file;
        }
        throw new IOException("Not a valid write permitted path : " + fileName);
    }

    protected File getTrustedValidWritableFile(String fileName, String trustedDir) throws IOException {
        File file = verifyParent(getTrustedValidWritableFile(fileName), trustedDir);
        if (file == null) {
            throw new IOException("Not a valid or permitted path : " + fileName);
        }
        return file;
    }

    private File verifyParent(File file, String trustedDir) throws IOException {
        if (trustedDir != null) {
            File trustedPath = new File(trustedDir);
            if (file != null && file.getCanonicalPath().startsWith(trustedPath.getCanonicalPath())) {
                return file;
            }
        }
        return null;
    }
}
