package com.hackorama.plethora.server.data.meta.cluster;

import java.util.HashMap;
import java.util.Map;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.config.ClusterConfiguration;
import com.hackorama.plethora.config.MetaConfiguration;

/**
 * The module that provides data about the metrics server itself as a set of metrics including all client end points for
 * the supported protocols and peer metrics server data if this is part of a metrics server cluster.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class Cluster {

    private static final String LIST_SEPARATOR = ",";
    private static final String HOST_PORT_SEPARATOR = ":";

    private final ClusterConfiguration clusterConfiguration;
    private final MetaConfiguration metaConfiguration;
    private final Map<String, ClusterMember> clusterMembers = new HashMap<String, ClusterMember>();
    private ClusterService clusterService;
    private String memberListString;
    private long lastPolledTimeMillis;

    public Cluster(ClusterConfiguration clusterConfiguration) {
        this(clusterConfiguration, null);
    }

    public Cluster(ClusterConfiguration clusterConfiguration, ClusterService clusterService) {
        this.clusterConfiguration = clusterConfiguration;
        this.metaConfiguration = clusterConfiguration.getMetaConfiguration();
        this.clusterService = clusterService;
        memberListString = "";
        lastPolledTimeMillis = 0;
        getStaticDefinedMembers(); // static data, read only once
        buildMemberList();
    }

    public String getName() {
        return clusterConfiguration.getClusterName();
    }

    public String getMemberList() {
        if (getDynamicDiscoveredMembers()) {
            buildMemberList(); // rebuild list with new discovered members
        }
        return memberListString;
    }

    public void setService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    private void buildMemberList() {
        StringBuilder builder = new StringBuilder();
        for (ClusterMember member : clusterMembers.values()) {
            builder.append(member.getHostname());
            builder.append(HOST_PORT_SEPARATOR);
            // if no port defined for a cluster member
            // default to the http port defined for this member instance
            builder.append(member.getPort() > 0 ? member.getPort() : metaConfiguration.getHttpPort());
            builder.append(LIST_SEPARATOR);
        }
        if (builder.lastIndexOf(LIST_SEPARATOR) >= 0) { // remove last
            builder.replace(builder.lastIndexOf(LIST_SEPARATOR), builder.length(), "");
        }
        memberListString = builder.toString();
    }

    private void getStaticDefinedMembers() {
        String memberlist = clusterConfiguration.getClusterMemberList();
        if (memberlist != null) {
            String[] memberitems = memberlist.split(LIST_SEPARATOR);
            for (String memberitem : memberitems) {
                String[] items = memberitem.split(HOST_PORT_SEPARATOR);
                if (items.length >= 1) {
                    String hostname = items[0];
                    int port = 0;
                    if (items.length >= 2) {
                        port = Util.getInt(items[1], 0);
                    }
                    clusterMembers.put(hostname, new ClusterMember(hostname, port));
                }
            }
        }
    }

    private boolean getDynamicDiscoveredMembers() {
        if (serviceAvailable() && updatedSinceLastPoll()) {
            Map<String, ClusterMember> discoveredMembers = clusterService.getMemberList();
            if (validMemberList(discoveredMembers)) {
                clusterMembers.putAll(discoveredMembers);
                return true;
            }
        }
        return false;
    }

    private boolean validMemberList(Map<String, ClusterMember> memberList) {
        return memberList != null && memberList.size() > 0;
    }

    private boolean serviceAvailable() {
        return clusterService != null;
    }

    private boolean updatedSinceLastPoll() {
        if (serviceAvailable()) {
            long lastUpdatedTimeMillis = clusterService.getLastUpdatedTimeMills();
            // if updated after last poll
            boolean updated = lastUpdatedTimeMillis > lastPolledTimeMillis;
            lastPolledTimeMillis = System.currentTimeMillis();
            return updated;
        }
        return false;
    }

}
