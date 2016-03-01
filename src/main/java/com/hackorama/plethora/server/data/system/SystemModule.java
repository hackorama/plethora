package com.hackorama.plethora.server.data.system;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC.TYPE;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.config.SystemConfiguration;
import com.hackorama.plethora.server.data.common.LocalModule;

/**
 * Provides system level metrics
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class SystemModule extends LocalModule {

    private static class NameTypePair {
        public String name;
        public String type;
    }

    // pidMap has concurrent access from main and process scan task threads
    private ConcurrentHashMap<String, Long> pidMap = new ConcurrentHashMap<String, Long>();
    private static final Logger LOG = Log.getLogger();
    private static final char SEPARATOR = '.';
    private static final long MIN_CACHING_SECS = 5;
    private ProcessMapper procMapper;
    private final SystemAccess sysAccess;
    private long lastScanMillis;
    private long scanIntervalMillis;

    public SystemModule(Metrics metrics, SystemConfiguration configuration) {
        this(metrics, configuration, null, null);
    }

    public SystemModule(Metrics metrics, SystemConfiguration configuration, SystemAccess systemAccess) {
        this(metrics, configuration, systemAccess, null);
    }

    public SystemModule(Metrics metrics, SystemConfiguration configuration, SystemAccess systemAccess,
            ProcessMapper processMapper) {
        super(metrics, "system");
        sysAccess = systemAccess != null ? systemAccess : initSystemAccess(configuration);
        if (sysAccess.isAvailable()) {
            procMapper = processMapper != null ? processMapper : initProcessMap(configuration);
            discoverMetrics();
        } else {
            LOG.severe("System access library " + sysAccess.getName() + " not available, no system metrics initialized");
        }
    }

    private void addMetric(String name, SYSTEM_METRIC_TYPE type, long pid) {
        addMetric(metricName(name, type), buildProperties(name, "System metric " + type + " for " + name));
    }

    private void addMetric(SYSTEM_METRIC_TYPE type) {
        String name = type.name();
        addMetric(name, buildProperties(name, "System metric " + name));
    }

    private MetricProperties buildProperties(String displayName, String description) {
        // all metrics in system module will be of type number
        return new MetricProperties().displayname(displayName).description(description).type(TYPE.NUMBER);
    }

    @Override
    protected void discoverMetrics() {
        // the system level metrics
        for (SYSTEM_METRIC_TYPE type : SYSTEM_METRIC_TYPE.values()) {
            addMetric(type);
        }
        // make a local copy of the map from the mapper
        pidMap = new ConcurrentHashMap<String, Long>(procMapper.getMap());
        // the process level metrics
        for (Entry<String, Long> entry : pidMap.entrySet()) {
            long pid = entry.getValue();
            for (SYSTEM_METRIC_TYPE type : SYSTEM_METRIC_TYPE.values()) {
                addMetric(entry.getKey(), type, pid);
            }
        }
    }

    private NameTypePair getNameTypePair(String value, char separator) {
        NameTypePair pair = new NameTypePair();
        if (value == null) {
            return pair;
        }
        int index = value.indexOf(separator);
        if (index > 0) { // check for pair
            pair.name = value.substring(0, index);
            pair.type = value.substring(index + 1);
        } else { // not a pair then value is considered type
            pair.type = value;
        }
        return pair;
    }

    private SYSTEM_METRIC_TYPE getType(String name) {
        if (name != null) {
            try {
                return SYSTEM_METRIC_TYPE.valueOf(name);
            } catch (IllegalArgumentException ignoreException) {
                // return null for invalid enum name
            }
        }
        return null;
    }

    @Override
    public Object getValue(String metric) {
        NameTypePair nameTypePair = getNameTypePair(metric, SEPARATOR);
        String name = nameTypePair.name;
        SYSTEM_METRIC_TYPE type = getType(nameTypePair.type);
        if (name != null && type != null) { // name.type
            return getValue(name, type);
        } else if (type != null) { // type
            return getValue(type);
        }
        LOG.warning("Ignoring invalid system metric " + metric);
        return null;
    }

    private Object getValue(String name, SYSTEM_METRIC_TYPE type) {
        Long pid = pidMap.get(name);
        if (pid == null) {
            LOG.warning("Ignoring unknown system metric : " + name);
        } else if (pid > 0) {
            try {
                return sysAccess.getMetric(type, pid);
            } catch (SystemAccessException e) {
                String msg = "Failed " + type + " access for " + name + " " + pid;
                LOG.warning(msg + ", " + e.getMessage());
                LOG.log(Level.FINEST, msg, e);
            }
        }
        return null;
    }

    private Object getValue(SYSTEM_METRIC_TYPE type) {
        try {
            return sysAccess.getMetric(type);
        } catch (SystemAccessException e) {
            String msg = "Failed " + type + " access";
            LOG.warning(msg + ", " + e.getMessage());
            LOG.log(Level.FINEST, msg, e);
        }
        return null;
    }

    private ProcessMapper initProcessMap(SystemConfiguration configuration) {
        return new PropertiesProcessMapper(configuration.getSystemPropertyFile(), sysAccess);
    }

    private SystemAccess initSystemAccess(SystemConfiguration configuration) {
        long dataRefresh = configuration.getSystemDataRefreshSecs();
        long dataCaching = configuration.getSystemDataCachingSecs();
        if (dataCaching < dataRefresh) {
            LOG.warning("Updating system data caching from " + dataCaching + " to " + dataRefresh
                    + " seconds, matching the data refresh rate");
            dataCaching = dataRefresh;
        }
        if (dataCaching < MIN_CACHING_SECS) {
            LOG.warning("System data caching " + dataCaching + ", is below recommended minimum " + MIN_CACHING_SECS
                    + " seconds ");
        }
        return new SigarFacade(dataCaching);
    }

    private boolean inValidProcs() {
        return pidMap.isEmpty() || pidMap.values().contains((long) 0);
    }

    private String metricName(String name, SYSTEM_METRIC_TYPE type) {
        return name + SEPARATOR + type.name();
    }

    @Override
    public boolean pokeModule() {
        if (inValidProcs()) {
            rescanProcessMap();
        }
        return true;
    }

    private void rescanProcessMap() {
        long thisScan = System.currentTimeMillis();
        if (thisScan - lastScanMillis >= scanIntervalMillis) {
            lastScanMillis = thisScan;
            scanProcessMap();
        }
    }

    private void scanProcessMap() {
        ConcurrentHashMap<String, Long> newMap = procMapper.getMap();
        for (Entry<String, Long> entry : newMap.entrySet()) {
            String name = entry.getKey();
            long pid = entry.getValue();
            // 1. update existing processes
            if (pidMap.containsKey(name)) {
                pidMap.put(name, pid);
                LOG.fine("Updating process id for " + name + " to " + pid);
                // 2. notify new processes found
            } else {
                LOG.info("Found new process " + name + ", restart metric service to add");
            }
        }
        Set<String> removeNames = new HashSet<String>();
        for (String name : pidMap.keySet()) {
            if (!newMap.containsKey(name)) {
                removeNames.add(name);
            }
        }
        // 3. disable removed processes and notify
        for (String name : removeNames) {
            pidMap.put(name, (long) 0);
            LOG.info("Process " + name + " not available, restart metric service to remove");
        }
    }
}
