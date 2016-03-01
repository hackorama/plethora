package com.hackorama.plethora.server;

public interface MetricServer {
    boolean start();

    void stop();

    boolean updateNotify();

    String getName();
}
