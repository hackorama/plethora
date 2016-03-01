package com.hackorama.plethora.server.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricServer;
import com.sun.net.httpserver.HttpServer;

/**
 * The HTTP metric server
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
// @SuppressWarnings("restriction")
// ResourcePath Location Type Access restriction: The type HttpServer is not
// accessible due to restriction on required library rt.jar
public class WebServer implements MetricServer, Callable<Object> {

    private static final String NAME = "HTTP web server";
    private static final int WAIT_TO_FINISH_SECS = 10;

    private final String host;
    private final int port;
    private HttpServer server;
    private final WebHandler webHandler;
    private final Executor executor;

    public WebServer(String host, int port, WebHandler webHandler) {
        this(host, port, webHandler, null);
    }

    public WebServer(String host, int port, WebHandler webHandler, Executor executor) {
        if (!Util.validPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        this.host = host;
        this.port = port;
        this.webHandler = webHandler;
        this.executor = executor;
    }

    @Override
    public boolean start() {
        String msg = NAME + " at " + host + ":" + port;
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "Failed to start " + msg + ", IO error", e);
            return false;
        }
        server.createContext("/", webHandler);
        server.setExecutor(executor); // null creates a default executor
        server.start();
        Log.getLogger().info("Started " + msg);
        return true;
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(WAIT_TO_FINISH_SECS);
            Log.getLogger().info("Stopped " + NAME);
        }
    }

    @Override
    public Object call() throws Exception {
        return start();
    }

    @Override
    public boolean updateNotify() {
        return true; // web server does not keep any data model to update
    }

    @Override
    public String getName() {
        return "Web server";
    }
}
