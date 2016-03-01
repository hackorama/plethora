package com.hackorama.plethora.server.data.meta.cluster;

import java.util.Map;

public interface ClusterService {
    Map<String, ClusterMember> getMemberList();

    long getLastUpdatedTimeMills();
}
