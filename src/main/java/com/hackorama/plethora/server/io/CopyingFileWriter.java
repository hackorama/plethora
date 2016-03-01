package com.hackorama.plethora.server.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

/**
 * File writing with optional defensive copy and renaming of file
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class CopyingFileWriter implements CheckedFileWriter {

    // TODO : using apache or guava file utils for more robust implementation

    public CopyingFileWriter() {
    }

    /**
     * Write a string to the file. Will attempt a defensive write by copy first and if it fails attempt regular direct
     * write. prefers correctness over performance.
     * 
     * @param file
     *            The file to write to
     * @param str
     *            The string to write to the file
     * @return false if the write failed, true otherwise or if there was nothing to write
     * @throws IOException
     *             If an I/O error occurs or for invalid file object input
     */
    @Override
    public boolean fileWrite(File file, String str) throws IOException {
        if (str == null || str.length() < 1) {
            return true;
        }
        if (file == null) {
            throw new IOException("Not a valid file or nothing to write");
        }
        try {
            return checkedWrite(file, str);
        } catch (IOException e) {
            Log.getLogger().log(Level.WARNING, "Checked write failed ", e);
        }
        return uncheckedWrite(file, str);
    }

    /**
     * Write directly to the file without any special defensive copying first.
     */
    private boolean uncheckedWrite(File file, String str) throws IOException {
        if (str == null || str.length() < 1) {
            return true;
        }
        if (file == null) {
            throw new IOException("Not a valid file or nothing to write");
        }
        if (!file.canWrite()) {
            throw new IOException("Not a valid file with write permission");
        }
        // no buffered write
        OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(file), Util.getEncoding());
        try {
            streamWriter.write(str);
            return true;
        } finally {
            Util.close(streamWriter, Log.getLogger());
        }
    }

    /**
     * Attempt a defensive write to the file by writing to a temp file first and then either do a renaming operation or
     * by copying the file contents.
     */
    private boolean checkedWrite(File file, String str) throws IOException {
        if (str == null || str.length() < 1) {
            return true;
        }
        if (file == null) {
            throw new IOException("Not a valid file");
        }
        File tempFile = File.createTempFile(".antn-tmp.", null); // ".tmp"
        if (!tempFile.canRead() || !file.canWrite()) {
            throw new IllegalArgumentException("Invalid file permissions");
        }
        OutputStreamWriter streamWriter = null;
        try {
            // no buffered write
            streamWriter = new OutputStreamWriter(new FileOutputStream(tempFile), Util.getEncoding());
            streamWriter.write(str);
        } finally {
            Util.close(streamWriter, Log.getLogger());
        }
        return fileCopy(tempFile, file);
    }

    /**
     * Copy the contents of a given file to a new file.
     * 
     * First attempts the file copy using a rename operation, which could be an atomic operation on many (not all)
     * platforms. If rename fails (in JDK 6.x rename may not work across file systems ) try a copy using NIO byte
     * channel transfer.
     * 
     */
    private boolean fileCopy(File sourceFile, File destFile) throws IOException {
        // prefer copy by file rename
        if (sourceFile.renameTo(destFile)) {
            return true;
        }
        // preferred method failed retry with file copy
        return fileCopyByTransfer(sourceFile, destFile);
    }

    /**
     * Copy the contents of a given file to a new file using NIO byte channel transfer
     */
    private boolean fileCopyByTransfer(File sourceFile, File destFile) throws IOException {
        // NIO based file copy
        if (sourceFile == null || destFile == null) {
            throw new IOException("Not a valid source or destination file");
        }
        if (!sourceFile.canRead() || !destFile.canWrite()) {
            throw new IOException("Invalid file permssions for source or destination file");
        }
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
        return false;
    }
}
