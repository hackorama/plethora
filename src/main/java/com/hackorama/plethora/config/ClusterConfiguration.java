package com.hackorama.plethora.config;

public interface ClusterConfiguration {
    String getClusterName();

    String getClusterMemberList();

    MetaConfiguration getMetaConfiguration();
}
