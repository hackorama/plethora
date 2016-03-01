package com.hackorama.plethora.server.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.web.handler.DynamicPageHandler;
import com.hackorama.plethora.server.web.handler.StaticPageHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * The HTTP dynamic metric request and static page request handler
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
// @SuppressWarnings("restriction")
public class WebHandler implements HttpHandler {

    private static final String ERROR_MSG = "ERROR";
    private final StaticPageHandler staticPageHandler;
    private final DynamicPageHandler dynamicPageHandler;

    public WebHandler(DynamicPageHandler dynamicPageHandler, StaticPageHandler staticPageHandler) {
        this.staticPageHandler = staticPageHandler;
        this.dynamicPageHandler = dynamicPageHandler;
    }

    public WebHandler(DynamicPageHandler dynamicPageHandler) {
        this(dynamicPageHandler, null);
    }

    public WebHandler(StaticPageHandler staticPageHandler) {
        this(null, staticPageHandler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Log.getLogger().finest(exchange.getRequestURI().getPath());
        byte[] response = getResponse(exchange.getRequestURI().getPath());
        exchange.sendResponseHeaders(200, response.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(response);
        stream.close();
    }

    private byte[] getResponse(String path) throws UnsupportedEncodingException {
        String sanitizedPath = sanitizePath(path);
        byte[] response = ERROR_MSG.getBytes(Util.getEncoding());
        if (dynamicPageHandler != null) {
            response = dynamicPageHandler.handleRequest(sanitizedPath);
        }
        if (response == null && staticPageHandler != null) {
            response = staticPageHandler.handleRequest(sanitizedPath);
        }
        return response;
    }

    private String sanitizePath(String path) { // TODO tighten
        return path == null ? null : path.replace('<', ' ').replace('>', ' ');
    }

}