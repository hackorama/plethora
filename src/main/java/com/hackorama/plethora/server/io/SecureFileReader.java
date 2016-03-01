package com.hackorama.plethora.server.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;

/**
 * Secured common file read operations after verifying the file paths are allowed by the access permissions defined in
 * FileAccessPermission.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class SecureFileReader extends SecureFileAccess {

    // TODO : using apache or guava file utils for more robust implementation

    public SecureFileReader(FileAccessPermission accessPermission) {
        super(accessPermission);
    }

    /**
     * 
     * @param fileName
     *            the source file name
     * @return the file contents as a byte array
     * @throws IOException
     *             If an I/O error occurs
     */
    public byte[] read(String fileName) throws IOException {
        if (fileName == null) {
            throw new IOException("Not a valid path");
        }
        return fileRead(getTrustedValidReadableFile(fileName));
    }

    public byte[] read(String fileName, String trustedDir) throws IOException {
        return fileRead(getTrustedValidReadableFile(fileName, trustedDir));
    }

    private byte[] fileRead(File file) throws IOException {
        if (file == null) {
            throw new IOException("Not a valid file object");
        }
        try {
            return streamRead(file);
        } catch (IOException e) {
            Log.getLogger().log(Level.WARNING, "Unchecked read failed ", e);
        }
        return mappedRead(file);
    }

    private byte[] streamRead(File file) throws IOException {
        byte[] data = new byte[(int) file.length()]; // can have race condition
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(data);
        } catch (IndexOutOfBoundsException e) { // in case of race condition
            throw new IOException("File read failed because of concurrent modification", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }

    private byte[] mappedRead(File file) throws IOException {
        FileInputStream stream = null;
        FileChannel channel = null;
        try {
            stream = new FileInputStream(file);
            channel = stream.getChannel();
            // TODO optimize read directly into byte array than buffer ?
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes, 0, bytes.length);
            return bytes;
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
    }
}
