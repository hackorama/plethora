package com.hackorama.plethora.server.annotations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.List;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

public class DocWriter {

    private final String docFileName;
    private final DocFormatter docFormatter;

    public DocWriter(String docFileName, DocFormatter docFormatter) {
        this.docFileName = docFileName;
        this.docFormatter = docFormatter;
    }

    public void write(List<? extends Annotation> annotations) throws IOException {
        if (annotations != null && annotations.size() > 0) {
            BufferedWriter writer = new BufferedWriter(openWriter(docFileName));
            docFormatter.format(annotations, writer);
            closeWriter(writer);
        }
    }

    private Writer openWriter(String path) throws IOException {
        return path == null ? null : new OutputStreamWriter(new FileOutputStream(new File(path)), Util.getEncoding());
    }

    private void closeWriter(BufferedWriter writer) throws IOException {
        if (writer != null) {
            writer.flush();
            Util.close(writer, Log.getLogger());
        }
    }

}
