package com.hackorama.plethora.server.io;

import java.io.File;
import java.io.IOException;

/**
 * File writer that will do additional verification checks
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public interface CheckedFileWriter {
    boolean fileWrite(File file, String str) throws IOException;
}
