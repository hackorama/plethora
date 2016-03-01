package com.hackorama.plethora.server.data.meta.cluster;

public final class ClusterMember {
    private final String hostname;
    private int port;
    private String descriptiveName;

    public ClusterMember(String hostname) {
        this.hostname = hostname;
        this.port = 0;
        this.descriptiveName = "";
    }

    public ClusterMember(String hostname, int port) {
        this(hostname);
        this.port = port;
    }

    public ClusterMember(String hostname, int port, String name) {
        this(hostname, port);
        this.descriptiveName = name;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getDescriptiveName() {
        return descriptiveName;
    }
}
