package com.hackorama.plethora.server.web.handler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.io.SecureFileReader;

public class StaticPageHandler {

    protected static final String ERROR_MSG = "<html>Please verify the web server configuration, cannot find required html content, please set up web.root in application.properties</html>";
    private static final String INDEX_FILE = "index.html";
    private static final String TMPL_DIR = "tmpl";
    private static final String HEADER_FILE = TMPL_DIR + File.separatorChar + "header.html";
    private static final String FOOTER_FILE = TMPL_DIR + File.separatorChar + "footer.html";
    private static final String ERROR_FILE = TMPL_DIR + File.separatorChar + "error.html";

    private final String webRoot;
    private final SecureFileReader secureFileReader;

    protected final byte[] HEADER;
    protected final byte[] FOOTER;
    protected final byte[] ERROR;

    public StaticPageHandler(String webRoot, SecureFileReader secureFileReader) {
        this.webRoot = webRoot;
        this.secureFileReader = secureFileReader;
        HEADER = checkedReadFileContents(resolvedPath(HEADER_FILE));
        FOOTER = checkedReadFileContents(resolvedPath(FOOTER_FILE));
        ERROR = checkedReadErrorFileContents(resolvedPath(ERROR_FILE));
    }

    public byte[] handleRequest(String requestPath) {
        // respond with index file for empty request
        String path = requestPath;
        if ("/".equals(path) || path.length() < 1) {
            path = INDEX_FILE;
        }
        return checkedReadFileContents(resolvedPath(path));
    }

    private byte[] getHeader(String title) {
        return HEADER.clone();
    }

    private byte[] getFooter() {
        return FOOTER.clone();
    }

    private static String templateFilter(String input, Map<String, String> filters) {
        return input;
    }

    private final Map<String, byte[]> pageCache = new HashMap<String, byte[]>();
    private final Map<String, Long> pageCacheInsertTime = new HashMap<String, Long>();

    private boolean isModifiedSince(String path) {
        // NOTE: accessing directly without security check since all paths
        // in the cache had been verified by SecureFileReader on first read
        // in to the cache. In addition there is no read/write of file
        // content.
        long lastModified = new File(path).lastModified();
        if (lastModified <= 0) { // on error, assume modified
            return true;
        } else {
            return lastModified >= pageCacheInsertTime.get(path);
        }
    }

    private byte[] cachedCheckedReadFileContents(String path) {
        if (pageCache.containsKey(path) && !isModifiedSince(path)) {
            return pageCache.get(path);
        }
        byte[] page = readFileContents(path);
        if (page != null) {
            pageCache.put(path, page);
            pageCacheInsertTime.put(path, new Date().getTime());
            return page;
        }
        return ERROR;
    }

    private final byte[] checkedReadFileContents(String path) {
        byte[] page = readFileContents(path);
        return page == null ? ERROR : page;
    }

    private final byte[] checkedReadErrorFileContents(String path) {
        byte[] page = readFileContents(path);
        byte[] error;
        try {
            error = ERROR_MSG.getBytes(Util.getEncoding());
        } catch (UnsupportedEncodingException e) {
            error = ERROR_MSG.getBytes(); // retry with default encoding
        }
        return page == null ? error : page;
    }

    private final byte[] readFileContents(String path) {
        try {
            return secureFileReader.read(path, webRoot);
        } catch (IOException e) {
            Log.getLogger().log(Level.FINE, "Error reading file : " + path, e);
        }
        return ERROR;
    }

    private String resolvedPath(String path) {
        return webRoot + File.separatorChar + path.trim();
    }
}
