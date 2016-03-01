package com.hackorama.plethora.server.web.handler;

import java.io.UnsupportedEncodingException;

public interface RequestHandler {
    byte[] getResponse(String path, DATA_TYPE type) throws UnsupportedEncodingException;

    byte[] getResponse(String path) throws UnsupportedEncodingException;

    String getName();
}
