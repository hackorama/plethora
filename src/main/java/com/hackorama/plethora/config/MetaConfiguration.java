package com.hackorama.plethora.config;

public interface MetaConfiguration {

    String getModuleName();

    String getServerName();

    String getServerVersion();

    String getServerReleaseDate();

    String getHttpHostname();

    int getHttpPort();

    String getJmxHostname();

    int getJmxPort();

    String getSnmpHostname();

    int getSnmpPort();

    String getClusterHostname();

    int getClusterPort();

    String getProductName();

    ClusterConfiguration getClusterConfiguration();
}
