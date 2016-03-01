package com.hackorama.plethora.config;

import java.io.File;

import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.annotations.PropertyDoc;

/**
 * The plethora server configuration implementation
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class ServerConfiguration extends ProtectedConfiguration implements MetaConfiguration,
        SystemConfiguration, ClusterConfiguration, SnmpConfiguration, CryptoConfiguration {

    private static final String VERSION = "0.1";
    private static final String COPYRIGHT = "Hackorama";
    private static final String DEF_PRODUCT_NAME = "Plethora";

    private static final String SNMP_MIB_FILE = "mib";
    private static final String SNMP_MIB_LOCATION = "snmp";
    private static final String KEY_FILE_PROP_NAME = "crypto.file";
    private static final String PROTECTED_PROP_MARKER = "protected.";

    private String installRoot;

    public ServerConfiguration(String file) {
        super(file);
        init();
    }

    public ServerConfiguration(String[] argv) {
        super(argv);
        init();
    }

    public ServerConfiguration(String file, Protector protector) throws Exception {
        super(file, protector);
        init();
    }

    public ServerConfiguration(String[] argv, Protector protector) throws SecurityException {
        super(argv, protector, KEY_FILE_PROP_NAME, PROTECTED_PROP_MARKER);
        init();
    }

    private void init() {
        getServerProperties();
    }

    private void getEnv() {
        installRoot = Util.getEnvPropValue("PLETHORA_INSTALL_ROOT", "plethora.install.root", installRoot);
    }

    private void getServerProperties() {
        getEnv();
        if (super.properties == null) {
            throw new IllegalArgumentException("No configuration found");
        }
        getRequiredServerProperties();
    }

    /* meta configuration */

    private void getRequiredServerProperties() {
        installRoot = getRequiredProperty("install.root", installRoot);
    }

    private String getRequiredProperty(String property) {
        String value = getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Missing required property " + property + " in configuration file");
        }
        return value;
    }

    private String getRequiredProperty(String property, String def) {
        String value = getProperty(property, def);
        if (value == null) {
            throw new IllegalArgumentException("Missing required property " + property + " in configuration file");
        }
        return value;
    }

    private int getRequiredNumberProperty(String property) {
        // throws runtime IllegalArgumentException
        return Util.getInt(getRequiredProperty(property), 0);
    }

    private Number getNumberProperty(String property) {
        return Util.getNumber(getProperty(property));
    }

    private Number getNumberProperty(String property, Number def) {
        return Util.getNumber(getProperty(property), def);
    }

    private int getIntegerProperty(String property) {
        return getIntegerProperty(property, 0);
    }

    private int getIntegerProperty(String property, int def) {
        return getNumberProperty(property, def).intValue();
    }

    private long getLongProperty(String property) {
        return getLongProperty(property, 0);
    }

    private long getLongProperty(String property, long def) {
        return getNumberProperty(property, def).longValue();
    }

    private String getPlethoraProperty(String name) {
        return getProperty("plethora." + name);
    }

    private String getPlethoraProperty(String name, String def) {
        return getProperty("plethora." + name, def);
    }

    private boolean getBooleanProperty(String property) {
        // only if set to "true" all other values or no value will be false
        if ("true".equalsIgnoreCase(getProperty(property, "false"))) {
            return true;
        }
        return false;
    }

    private boolean getBooleanProperty(String property, Boolean def) {
        return getProperty(property) != null ? getBooleanProperty(property) : def;
    }

    private int getInt(String value) {
        return Integer.parseInt(value);
    }

    private long getLong(String value) {
        return Long.parseLong(value);
    }

    /* meta configuration */

    private final static String WEB_ROOT = "web.root";

    @Override
    @PropertyDoc(name = WEB_ROOT, defaultValue = "web", doc = "The location of the static web content, "
            + "defaults to location relative to install root")
    public String getWebRoot() {
        return getProperty(WEB_ROOT, installRoot + File.separatorChar + "web");
    }

    private final static String LOG_ROOT = "log.root";

    @PropertyDoc(name = LOG_ROOT, defaultValue = "log", doc = "Log file location, defaults to location relative to install root")
    public String getLogRoot() {
        return getProperty(LOG_ROOT, installRoot + File.separatorChar + "log");
    }

    private static final String DATA_POLL_INTERVAL = "data.refresh.seconds";
    private static final String DATA_POLL_INTERVAL_DEF = "5";

    @PropertyDoc(name = DATA_POLL_INTERVAL, defaultValue = DATA_POLL_INTERVAL_DEF, doc = "Defined in seconds, "
            + "how often metric data is collected from the application modules")
    public long getDataRefreshSeconds() {
        return getLongProperty(DATA_POLL_INTERVAL, getLong(DATA_POLL_INTERVAL_DEF));
    }

    private final static String CONN_POLL_INTERVAL = "connection.retry.seconds";
    private static final String CONN_POLL_INTERVAL_DEF = "60";

    @PropertyDoc(name = CONN_POLL_INTERVAL, defaultValue = CONN_POLL_INTERVAL_DEF, doc = "Defined in seconds, "
            + "How often to poll for application module connections")
    public long getModuleConnectionRetryInterval() {
        return getLongProperty(CONN_POLL_INTERVAL, getLong(CONN_POLL_INTERVAL_DEF));
    }

    public boolean isHighPrecisionMode() {
        return getBooleanProperty("high.precision");
    }

    public boolean isJmxMBeanForceUpdated() {
        return getBooleanProperty("jmx.force.mbean.update");
    }

    @Override
    public MetaConfiguration getMetaConfiguration() {
        return this;
    }

    @Override
    public String getModuleName() {
        return "plethora";
    }

    @Override
    public String getProductName() {
        return getProperty("product.name", DEF_PRODUCT_NAME);
    }

    @Override
    public String getServerName() {
        return getPlethoraProperty("server.name", "Plethora Metrics Server");
    }

    @Override
    public String getServerVersion() {
        return getPlethoraProperty("version", VERSION); // TODO source from build
    }

    @Override
    public String getServerReleaseDate() {
        return getPlethoraProperty("release", ""); // TODO source from build
    }

    @Override
    public String getHttpHostname() {
        return getProperty("http.host");
    }

    @Override
    public int getHttpPort() {
        return this.getIntegerProperty("http.port");
    }

    private static final String HTTP_POOL_SIZE = "http.server.thread.pool.size";
    private static final String HTTP_POOL_SIZE_DEF = "2";

    @PropertyDoc(name = HTTP_POOL_SIZE, defaultValue = HTTP_POOL_SIZE_DEF, doc = " Thread pool size for HTTP metric server")
    public int getHttpThreadPoolSize() {
        return this.getIntegerProperty(HTTP_POOL_SIZE, getInt(HTTP_POOL_SIZE_DEF));
    }

    @Override
    public String getJmxHostname() {
        return getProperty("jmx.host");
    }

    @Override
    public int getJmxPort() {
        return getIntegerProperty("jmx.port");
    }

    @Override
    public String getSnmpHostname() {
        return getProperty("snmp.host");
    }

    @Override
    public int getSnmpPort() {
        return getIntegerProperty("snmp.port");
    }

    /* crypto key configuration */

    private static final String SNMP_POOL_SIZE = "snmp.server.thread.pool.size";
    private static final String SNMP_POOL_SIZE_DEF = "2";

    @PropertyDoc(name = SNMP_POOL_SIZE, defaultValue = SNMP_POOL_SIZE_DEF, doc = " Thread pool size for SNMP metric server")
    public int getSnmpThreadPoolSize() {
        return this.getIntegerProperty(SNMP_POOL_SIZE, getInt(SNMP_POOL_SIZE_DEF));
    }

    @Override
    public ClusterConfiguration getClusterConfiguration() {
        return this;
    }

    /* cluster configuration */

    @Override
    public String getClusterHostname() {
        return getProperty("cluster.host");
    }

    @Override
    public int getClusterPort() {
        return getIntegerProperty("cluster.port");
    }

    @Override
    public String getClusterName() {
        return getProperty("cluster.name");
    }

    @Override
    public String getClusterMemberList() {
        return getProperty("cluster.member.list");
    }

    /* SNMP configuration */

    @Override
    public String getSnmpEnterpriseSubTreeNumbers() {
        return getProperty("snmp.private.enterprise.subtree.as.numbers");
    }

    @Override
    public String getSnmpEnterpriseSubTreeNames() {
        return getProperty("snmp.private.enterprise.subtree.as.names");
    }

    @Override
    public String getSnmpMibFilePath() {
        return SNMP_MIB_LOCATION + File.separatorChar + SNMP_MIB_FILE;
    }

    @Override
    public String getSnmpMibUrlPath() {
        return SNMP_MIB_LOCATION + '/' + SNMP_MIB_FILE;
    }

    @Override
    public String getSnmpSysDescr() {
        return getProperty("snmp.sysdecr", "Plethora Metrics Server Agent Version " + VERSION + " " + COPYRIGHT);
    }

    @Override
    public String getSnmpSysContact() {
        return getProperty("snmp.syscontact", "");
    }

    @Override
    public String getSnmpSysName() {
        return getProperty("snmp.sysname"); // no default
    }

    @Override
    public String getSnmpSysLocation() {
        return getProperty("snmp.syslocation", ""); // default to empty
    }

    @Override
    public String getSnmpMibUrl() {
        if (Util.validNonEmpty(getHttpHostname()) && Util.validPort(getHttpPort())) {
            return "http://" + getHttpHostname() + ":" + getHttpPort() + "/" + getSnmpMibUrlPath();
        }
        return null;
    }

    /* crypto key configuration */

    @Override
    public String getKeyFileName() {
        return getProperty(KEY_FILE_PROP_NAME);
    }

    @Override
    public String getKeyPassword() {
        return getProperty(PROTECTED_PROP_MARKER + "crypto.passwd");
    }

    @Override
    public String getKeyType() {
        return getProperty("crypto.type");
    }

    public SystemConfiguration getSystemConfiguration() {
        return this;
    }

    @Override
    public String getSystemPropertyFile() {
        return getProperty("system.process.map");
    }

    @Override
    public long getSystemDataRefreshSecs() {
        return getDataRefreshSeconds();
    }

    private static final String SYS_PROC_SCAN_SECS = "system.process.rescan.secs";
    private static final String SYS_PROC_SCAN_SECS_DEF = "600";

    @Override
    @PropertyDoc(name = SYS_PROC_SCAN_SECS, defaultValue = SYS_PROC_SCAN_SECS_DEF, doc = "Defined in seconds, "
            + "how often to rescan for application process ids, in case they have restarted with a new pid")
    public long getSysProcessRescanSecs() {
        return getLongProperty(SYS_PROC_SCAN_SECS, getLong(SYS_PROC_SCAN_SECS_DEF));
    }

    private static final String SYS_CACHE_SECS = "system.data.caching.secs";
    private static final String SYS_CACHE_SECS_DEF = "30";

    @Override
    @PropertyDoc(name = SYS_CACHE_SECS, defaultValue = SYS_CACHE_SECS, doc = "Defined in seconds, "
            + "how long to cache the system metrics collected. "
            + "Higher value reduces any system metric collection overhead")
    public long getSystemDataCachingSecs() {
        return getLongProperty(SYS_CACHE_SECS, getLong(SYS_CACHE_SECS_DEF));
    }

    private static final String SYS_ENABLED = "system.metrics";
    private static final String SYS_ENABLED_DEF = "true";

    @PropertyDoc(name = SYS_ENABLED, defaultValue = SYS_ENABLED_DEF, doc = "Enables or disables system metrics collection")
    @Override
    public boolean isSystemMetricsEnabled() {
        return getBooleanProperty(SYS_ENABLED, Boolean.valueOf(SYS_ENABLED_DEF));
    }

}
