package com.hackorama.plethora.config;

public interface SystemConfiguration {

    String getSystemPropertyFile();

    long getSystemDataRefreshSecs();

    long getSystemDataCachingSecs();

    long getSysProcessRescanSecs();

    boolean isSystemMetricsEnabled();

}
