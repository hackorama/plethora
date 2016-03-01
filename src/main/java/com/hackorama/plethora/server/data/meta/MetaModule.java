package com.hackorama.plethora.server.data.meta;

import java.util.HashMap;
import java.util.Map;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC.TYPE;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.config.MetaConfiguration;
import com.hackorama.plethora.server.data.common.LocalModule;
import com.hackorama.plethora.server.data.meta.cluster.Cluster;
import com.hackorama.plethora.server.data.meta.cluster.ClusterService;

/**
 * The module that provides data about the metrics server itself as a set of metrics including all client end points for
 * the supported protocols and peer metrics server data if this is part of a metrics server cluster.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class MetaModule extends LocalModule {

    private final Cluster cluster;
    private static final String CLUSTER_MEMBERS = "cluster_members";
    private final MetaConfiguration metaConfiguration;

    private final Map<String, Object> values = new HashMap<String, Object>();

    public MetaModule(Metrics metrics, MetaConfiguration metaConfiguration) {
        super(metrics, metaConfiguration.getModuleName()); // set module name
        this.metaConfiguration = metaConfiguration;
        cluster = new Cluster(metaConfiguration.getClusterConfiguration());
        discoverMetrics();
    }

    public void registerService(ClusterService clusterService) {
        cluster.setService(clusterService);
    }

    @Override
    public Object getValue(String metric) {
        if (CLUSTER_MEMBERS.equalsIgnoreCase(metric)) {
            updateClusterMetrics();
        }
        return values.get(metric);
    }

    @Override
    public boolean setValue(String metric, Object value) {
        if (values.containsKey(metric)) {
            values.put(metric, value);
            return true;
        }
        return false;
    }

    private void addMetric(String metricName, MetricProperties properties, Object value) {
        values.put(metricName, getNonNull(value, properties.getType()));
        addMetric(metricName, properties);
    }

    // all metrics in meta module will be of type text, since they are constants
    private MetricProperties buildProperties(String displayName, String description) {
        return new MetricProperties().displayname(displayName).description(description).type(TYPE.TEXT);
    }

    private void addServerMetrics() {
        String description = "The name of the this plethora metrics server instance";
        addMetric("server_name", buildProperties("Server Name", description), metaConfiguration.getServerName());

        description = "The version of the this plethora metrics server instance";
        addMetric("server_version", buildProperties("Server Version", description),
                metaConfiguration.getServerVersion());

        description = "The release date of the this plethora metrics server instance";
        addMetric("server_release_date", buildProperties("Server Release Date", description),
                metaConfiguration.getServerReleaseDate());

        description = "The HTTP hostname for this metrics server";
        addMetric("http_hostname", buildProperties("HTTP Hostname", description), metaConfiguration.getHttpHostname());
        description = "The HTTP listen point port number for this metrics server";
        addMetric("http_port", buildProperties("HTTP Port", description), metaConfiguration.getHttpPort());

        description = "The jmx hostname for this metrics server";
        addMetric("jmx_hostname", buildProperties("JMX Hostname", description), metaConfiguration.getJmxHostname());
        description = "The JMX port number for this metrics server";
        addMetric("jmx_port", buildProperties("JMX Port", description), metaConfiguration.getJmxPort());

        description = "The SNMP hostname for this metrics server";
        addMetric("snmp_hostname", buildProperties("SNMP Hostname", description), metaConfiguration.getSnmpHostname());
        description = "The SNMP port number for this metrics server";
        addMetric("snmp_port", buildProperties("SNMP Port", description), metaConfiguration.getSnmpPort());

        description = "The cluster hostname for this metrics server";
        addMetric("cluster_hostname", buildProperties("Cluster Hostname", description),
                metaConfiguration.getClusterHostname());
        description = "The cluster port number for this metrics server";
        addMetric("cluster_port", buildProperties("Cluster Port", description), metaConfiguration.getClusterPort());

    }

    private void updateClusterMetrics() {
        // only the dynamic member list need to be updated
        values.put(CLUSTER_MEMBERS, cluster.getMemberList());
    }

    private void addClusterMetrics() {
        if (cluster.getName() == null) {
            Log.getLogger().info(
                    "Not part of a cluster, no cluster information metrics added to " + moduleName + " module");
            return;
        }
        String description = "The name of the cluster this metrics server instance belongs to";
        addMetric("cluster_name", buildProperties("Cluster Name", description), cluster.getName());
        description = "Cluster member HTTP endpoints as a comma separated list of host:port";
        addMetric(CLUSTER_MEMBERS, buildProperties("Cluster Member List", description), cluster.getMemberList());
    }

    private Object getNonNull(Object value, TYPE type) {
        return value == null ? getDefaultValue(type) : value;
    }

    private Object getDefaultValue(TYPE type) {
        Object value = ""; // default to TEXT
        if (TYPE.BOOLEAN.equals(type)) {
            value = false;
        } else if (TYPE.NUMBER.equals(type)) {
            value = 0;
        }
        return value;
    }

    @Override
    protected void discoverMetrics() {
        addServerMetrics();
        addClusterMetrics();
    }

}
