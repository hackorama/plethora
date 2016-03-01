package com.hackorama.plethora.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.METRIC.TYPE;
import com.hackorama.plethora.common.data.Metric;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.common.data.NameResolver;
import com.hackorama.plethora.server.data.Modules;
import com.hackorama.plethora.server.data.common.Module;
import com.hackorama.plethora.server.data.meta.cluster.ClusterService;

public class MetricService {

    private final Modules modules;
    private final Metrics metrics;
    private final NameResolver resolver;
    private boolean immediateAccess;
    private final Map<String, METRIC.TYPE> typeMap = new HashMap<String, METRIC.TYPE>();
    private final List<MetricServer> metricServers = new ArrayList<MetricServer>();

    public MetricService(Modules modules) {
        immediateAccess = false; // default use fixed interval polled data
        this.modules = modules;
        metrics = modules.getMetrics();
        resolver = metrics.getResolver();
        initTypeMap();
    }

    public void enableImmediateMode() {
        immediateAccess = true;
        Log.getLogger().info("Metric service is now in high precision mode");
    }

    public void disableImmediateMode() {
        immediateAccess = false;
        Log.getLogger().info("Metric service is now in polling mode");
    }

    public Metrics getMetricsInstance() {
        return metrics;
    }

    public void initModules() {
        modules.init();
    }

    public Object getModule(int moduleIndex) {
        if (moduleIndex >= 0 && moduleIndex < modules.getModuleCount()) {
            return modules.getModuleList().toArray()[moduleIndex];
        }
        return null;
    }

    public int getModuleCount() {
        return modules.getModuleCount();
    }

    public Set<String> getModuleList() {
        return modules.getModuleList();
    }

    public int getModuleMetricCount(int moduleIndex) {
        if (moduleIndex >= 0 && moduleIndex < modules.getModuleCount()) {
            Module module = modules.getModule((String) modules.getModuleList().toArray()[moduleIndex]);
            if (module != null) {
                return module.getMetricCount();
            }
        }
        return 0;
    }

    public Object getMetric(String name) {
        return getMetric(metrics.getMetric(name));
    }

    public Object getMetric(int moduleIndex, int metricIndex) {
        if (moduleIndex >= 0 && metricIndex >= 0 && moduleIndex < modules.getModuleCount()) {
            String moduleName = (String) modules.getModuleList().toArray()[moduleIndex];
            // metric index lower limit checked already above, for early failure
            if (metricIndex < modules.getModule(moduleName).getMetricNames().size()) {
                String metricName = (String) modules.getModule(moduleName).getMetricNames().toArray()[metricIndex];
                return getMetric(resolver.resolveMetricName(metricName, moduleName));
            }
        }
        return null;
    }

    public Map<String, Object> getMetrics() {
        return getMetrics(null);
    }

    public Map<String, Object> getMetrics(String type) {
        Map<String, Object> result = new HashMap<String, Object>();
        METRIC.TYPE metricType = Util.invalidEmpty(type) ? null : getType(type);
        // fail early for non null non empty input but are invalid type value
        if (METRIC.TYPE.INVALID == metricType) {
            return result;
        }
        for (Entry<String, Metric<?>> item : metrics.getMetrics().entrySet()) {
            Metric<?> metric = item.getValue();
            if (metricType == null) {
                // return all metrics if type is set to empty or null
                result.put(metric.getName(), getMetric(metric));
            } else if (metricType == metric.getMetricType()) {
                // return metrics of matching valid known type
                result.put(metric.getName(), getMetric(metric));
            }
        }
        return result;
    }

    public Map<String, Object> getModuleMetrics(String module) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String metric : getModuleMetricNames(module)) {
            String name = resolver.resolveMetricName(metric, module);
            result.put(name, getMetric(name));
        }
        return result;
        /*
         * TODO: Check very minor performance diff between two O(n) versions A. 1 x hashmap get + n x string appends :
         * shorter n B. n x string equals : longer n A is the version used above and B is the version below
         * for(Entry<String, Metric<?>> item : metrics.getMetrics().entrySet()){ Metric<?> metric = item.getValue(); if
         * (module.equalsIgnoreCase(metric.getModuleName())) { result.put(metric.getName(), getMetric(metric)); } }
         */
    }

    public Set<String> getModuleMetricNames(String module) {
        Module theModule = modules.getModule(module);
        return theModule == null ? new TreeSet<String>() : theModule.getMetricNames();
    }

    public Set<String> getMetricList() {
        return metrics.getNames();
    }

    public Set<String> getMetricList(String type) {
        Set<String> result = new HashSet<String>();
        if (type == null) {
            return result;
        }
        METRIC.TYPE metricType = getType(type);
        for (Entry<String, Metric<?>> item : metrics.getMetrics().entrySet()) {
            Metric<?> metric = item.getValue();
            if (metricType == metric.getMetricType()) {
                result.add(metric.getName());
            }
        }
        return result;
    }

    public MetricProperties getMetricProperties(String moduleName, String metricName) {
        Metric<?> metric = metrics.getMetric(resolver.resolveMetricName(metricName, moduleName));
        return metric == null ? null : metric.getProperties();
    }

    public boolean setMetricValue(String name, Object value) {
        return metrics.setValue(name, value);
    }

    public boolean refreshMetric(String name) {
        Metric<?> metric = metrics.getMetric(name);
        if (metric == null) {
            return false;
        }
        return metric.setValueAsObject(modules.getValue(metric.getModuleName(), metric.getMetricName()));
    }

    public void refreshMetrics() {
        refreshMetrics(null);
    }

    public void refreshMetrics(String module) {
        for (Entry<String, Metric<?>> item : metrics.getMetrics().entrySet()) {
            Metric<?> metric = item.getValue();
            if (module == null) {
                // update all metrics
                refreshMetric(metric.getName());
            } else if (module.equalsIgnoreCase(metric.getModuleName())) {
                // update only metrics from specified module
                refreshMetric(metric.getName());
            }
        }
    }

    public void registerServer(MetricServer server) {
        interceptServerRegistration(server);
        metricServers.add(server);
    }

    public void pollModules() {
        if (modules.checkModulesForUpdates()) {
            notifyServers();
        }
        modules.pokeModules();
    }

    private void interceptServerRegistration(MetricServer server) {
        if (server instanceof ClusterService) {
            modules.getMetaModule().registerService((ClusterService) server);
        }
    }

    private void notifyServers() {
        for (MetricServer metricServer : metricServers) {
            metricServer.updateNotify();
        }
    }

    private final void initTypeMap() {
        typeMap.put("number", METRIC.TYPE.NUMBER);
        typeMap.put("text", METRIC.TYPE.TEXT);
        typeMap.put("boolean", METRIC.TYPE.BOOLEAN);
    }

    private Object getMetric(Metric<?> metric) {
        if (metric == null) {
            return null;
        }
        if (immediateAccess) {
            metric.setValueAsObject(modules.getValue(metric.getModuleName(), metric.getMetricName()));
        }
        return metric.getValue();
    }

    private TYPE getType(String param) {
        METRIC.TYPE type = typeMap.get(param);
        return type == null ? METRIC.TYPE.INVALID : type;
    }

}
