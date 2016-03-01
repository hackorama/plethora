package com.hackorama.plethora.server.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Keeps track of a white list of paths that are allowed for read access or read and write access. (no write only
 * access)
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class FileAccessPermission {

    private final List<String> readPaths;
    private final List<String> writePaths;

    public FileAccessPermission() {
        readPaths = new ArrayList<String>();
        writePaths = new ArrayList<String>();
    }

    public void addReadPermittedFile(String path) throws IOException {
        readPaths.add(resolveValidReadableFile(path));
    }

    public void addReadPermittedDirectory(String path) throws IOException {
        readPaths.add(resolveValidReadableDir(path));
    }

    public void addWritePermittedFile(String path) throws IOException {
        writePaths.add(resolveValidWritableFile(path));
    }

    public void addWritePermittedDirectory(String path) throws IOException {
        writePaths.add(resolveValidWritableDir(path));
    }

    public boolean isReadPermittedPath(String path) {
        return checkPath(path, readPaths);
    }

    public boolean isWritePermittedPath(String path) {
        return checkPath(path, writePaths);
    }

    public boolean isReadWritePermittedPath(String path) {
        return isReadPermittedPath(path) && isWritePermittedPath(path);
    }

    private boolean checkPath(String path, List<String> paths) {
        if (path != null) {
            for (String allowedPath : paths) {
                if (path.equals(allowedPath) || path.startsWith(allowedPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String resolveValidReadableFile(String path) throws IOException {
        if (path != null) {
            File file = new File(path);
            // file exists and readable
            if (file.exists() && file.canRead()) {
                return file.getCanonicalPath();
            } else {
                throw new IOException("Not a valid readable file : " + path);
            }
        }
        throw new IOException("Not a valid path");
    }

    private String resolveValidWritableFile(String path) throws IOException {
        if (path != null) {
            File file = new File(path);
            // file exists and writable
            if (file.exists() && file.canWrite()) {
                return file.getCanonicalPath();
            } else { // or parent dir exists and writable
                File parent = file.getParentFile();
                if (parent != null && parent.exists() && parent.canWrite()) {
                    return file.getCanonicalPath();
                }
            }
            throw new IOException("Not a valid writable file " + path);
        }
        throw new IOException("Not a valid path");
    }

    private String resolveValidReadableDir(String path) throws IOException {
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory() && file.canRead()) {
                return file.getCanonicalPath();
            } else {
                throw new IOException("Not a valid readable directory : " + path);
            }
        }
        throw new IOException("Not a valid path");
    }

    private String resolveValidWritableDir(String path) throws IOException {
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory() && file.canWrite()) {
                return file.getCanonicalPath();
            } else {
                throw new IOException("Not a valid writable directory : " + path);
            }
        }
        throw new IOException("Not a valid path");
    }
}
