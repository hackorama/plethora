package com.hackorama.plethora.server.io;

import java.io.IOException;

/**
 * Secured common file write operations after verifying the file paths are allowed by the access permissions defined in
 * FileAccessPermission.
 * 
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class SecureFileWriter extends SecureFileAccess {

    // TODO : using apache or guava file utils for more robust implementation

    CheckedFileWriter fileWriter;

    public SecureFileWriter(FileAccessPermission accessPermission) {
        super(accessPermission);
        fileWriter = new CopyingFileWriter();
    }

    public SecureFileWriter(FileAccessPermission accessPermission, CheckedFileWriter fileWriter) {
        super(accessPermission);
        this.fileWriter = fileWriter;
    }

    /**
     * Write the string to the destination file after checking if the file access is allowed by FileAccessPermission.
     * 
     * @param str
     *            The string whose contents will be written to file
     * @param fileName
     *            The destination file name
     * @return true if and only if the write succeeded; false otherwise
     * @throws IOException
     *             If an I/O error occurs
     */
    public boolean write(String fileName, String str) throws IOException {
        return fileWriter.fileWrite(getTrustedValidWritableFile(fileName), str);
    }

}
