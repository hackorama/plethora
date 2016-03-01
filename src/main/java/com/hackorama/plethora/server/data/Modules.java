package com.hackorama.plethora.server.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.config.ServerConfiguration;
import com.hackorama.plethora.server.data.common.Module;
import com.hackorama.plethora.server.data.http.Connector;
import com.hackorama.plethora.server.data.http.WebModule;
import com.hackorama.plethora.server.data.jmx.JMXModule;
import com.hackorama.plethora.server.data.meta.MetaModule;
import com.hackorama.plethora.server.data.system.SystemModule;

public final class Modules {

    private static final String PROPTERY_NAME_JMXMODULE = "jmxmodule.";
    private static final String PROPTERY_NAME_WEBMODULE = "webmodule.";
    private static final String PROPERTY_NAME_MODULE_HOST = ".host";
    private static final String PROPERTY_NAME_MODULE_PORT = ".port";
    private static final String PROPERTY_NAME_MODULE_PATH = ".path";

    private static final Metrics METRICS = Metrics.getInstance();
    private MetaModule metaModule;
    private final ServerConfiguration configuration;
    private int pendingModuleCount;

    private final Map<String, Module> MODULE_MAP = new HashMap<String, Module>();
    // Keep sorted module names from unsorted module map for ordered access
    private final Set<String> MODULE_NAMES = new TreeSet<String>();

    public Modules(ServerConfiguration configuration) {
        this.configuration = configuration;
        metaModule = null;
        pendingModuleCount = 0;
    }

    public void init() {
        initMetaModule();
        initSystemModule();
        initJMXModules(configuration);
        initWebModules(configuration);
        MODULE_NAMES.addAll(MODULE_MAP.keySet());
    }

    public Metrics getMetrics() {
        return METRICS;
    }

    public Object getValue(String moduleName, String metricName) {
        Module module = getModule(moduleName);
        if (module != null) {
            return module.getValue(metricName);
        }
        return null;
    }

    public boolean setValue(String moduleName, String metricName, Object value) {
        Module module = getModule(moduleName);
        if (module != null) {
            return module.setValue(metricName, value);
        }
        return false;
    }

    public boolean setValue(String moduleName, String metricName, long value) {
        Module module = getModule(moduleName);
        if (module != null) {
            return module.setValue(metricName, value);
        }
        return false;
    }

    public boolean setValue(String moduleName, String metricName, boolean value) {
        Module module = getModule(moduleName);
        if (module != null) {
            return module.setValue(metricName, value);
        }
        return false;
    }

    public boolean setValue(String moduleName, String metricName, String value) {
        Module module = getModule(moduleName);
        if (module != null) {
            return module.setValue(metricName, value);
        }
        return false;
    }

    public boolean checkModulesForUpdates() {
        int pending = 0;
        boolean updated = false;
        for (Module module : MODULE_MAP.values()) {
            if (!module.isConnected()) { // not connected modules
                String name = module.getName();
                Log.getLogger().finest(name + " is not ready");
                if (module.connect()) { // new connection worked
                    Log.getLogger().info(name + " is now connected");
                    updated = true; // update servers
                } else { // still not connected
                    Log.getLogger().warning(name + " retry connection failed ");
                    pending++;
                }
            }
        }
        pendingModuleCount = pending;
        return updated;
    }

    public void pokeModules() {
        for (Module module : MODULE_MAP.values()) {
            module.pokeModule();
        }
    }

    public Set<String> getModuleList() {
        return MODULE_NAMES;
    }

    public Module getModule(String moduleName) {
        return MODULE_MAP.get(moduleName);
    }

    public MetaModule getMetaModule() {
        return metaModule;
    }

    public int getModuleCount() {
        return MODULE_NAMES.size(); // same as MODULE_MAP size
    }

    public int getPendingModuleCount() {
        return pendingModuleCount;
    }

    public void onExit() {
        // TODO any clean up
    }

    private void initJMXModules(ServerConfiguration configuration) {
        if (configuration == null) {
            Log.getLogger().severe("Illegal argument, null configuration provided for jmx module");
            return;
        }
        Map<String, String> hosts = getConfiguration(configuration, PROPTERY_NAME_JMXMODULE, PROPERTY_NAME_MODULE_HOST);
        Map<String, String> ports = getConfiguration(configuration, PROPTERY_NAME_JMXMODULE, PROPERTY_NAME_MODULE_PORT);
        for (Entry<String, String> host : hosts.entrySet()) {
            String module = host.getKey();
            String hostname = host.getValue();
            int port = Util.getInt(ports.get(module), 0);
            initJMXModule(module, hostname, port);
        }
    }

    private void initJMXModule(String name, String hostname, int port) {
        Module module = null;
        try {
            module = new JMXModule(METRICS, name, hostname, port);
        } catch (IllegalArgumentException e) {
            Log.getLogger().severe("Illegal argumnets for jmx module " + name + ", " + e.getMessage());
            return;
        }
        MODULE_MAP.put(module.getName(), module);
        if (module.isConnected()) {
            Log.getLogger().info("JMX module " + name + " ready");
        } else {
            Log.getLogger().severe("JMX module " + name + " not available");
        }
    }

    private void initWebModules(ServerConfiguration configuration) {
        if (configuration == null) {
            Log.getLogger().severe("Illegal argument, null configuration provided for web module");
            return;
        }

        Map<String, String> hosts = getConfiguration(configuration, PROPTERY_NAME_WEBMODULE, PROPERTY_NAME_MODULE_HOST);
        Map<String, String> ports = getConfiguration(configuration, PROPTERY_NAME_WEBMODULE, PROPERTY_NAME_MODULE_PORT);
        Map<String, String> paths = getConfiguration(configuration, PROPTERY_NAME_WEBMODULE, PROPERTY_NAME_MODULE_PATH);
        for (Entry<String, String> host : hosts.entrySet()) {
            String module = host.getKey();
            String hostname = host.getValue();
            int port = Util.getInt(ports.get(module), 0);
            String path = paths.get(module);
            initWebModule(module, hostname, port, path);
        }
    }

    private void initWebModule(String name, String hostname, int port, String path) {
        Module module;
        try {
            module = new WebModule(name, METRICS, new Connector(name, hostname, port, path, configuration));
        } catch (IllegalArgumentException e) {
            Log.getLogger().severe("Illegal argumnets for web module " + name + ", " + e.getMessage());
            return;
        }
        MODULE_MAP.put(module.getName(), module);
        if (module.isConnected()) {
            Log.getLogger().info("Web module " + name + " ready");
        } else {
            Log.getLogger().severe("Web module " + name + " not available");
        }
    }

    private void initMetaModule() {
        metaModule = new MetaModule(METRICS, configuration.getMetaConfiguration());
        MODULE_MAP.put(metaModule.getName(), metaModule);
        Log.getLogger().info("Meta module " + metaModule.getName() + " ready");
    }

    private void initSystemModule() {
        if (configuration.isSystemMetricsEnabled()) {
            SystemModule systemModule = new SystemModule(METRICS, configuration.getSystemConfiguration());
            MODULE_MAP.put(systemModule.getName(), systemModule);
            Log.getLogger().info("System module " + systemModule.getName() + " ready");
        } else {
            Log.getLogger().info("System module is not enabled in configuration");
        }
    }

    private Map<String, String> getConfiguration(ServerConfiguration configuration, String ofType, String withProperty) {
        return configuration.getValuesStartingAndEndingWith(ofType, withProperty);
    }

}
