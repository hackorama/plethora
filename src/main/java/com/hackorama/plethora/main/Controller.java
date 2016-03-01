package com.hackorama.plethora.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.concurrent.TaskExecutors;
import com.hackorama.plethora.concurrent.TaskFactory;
import com.hackorama.plethora.concurrent.TaskThread;
import com.hackorama.plethora.config.ServerConfiguration;
import com.hackorama.plethora.server.MetricServer;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.data.Modules;
import com.hackorama.plethora.server.io.SecureFileReader;
import com.hackorama.plethora.server.io.SecureFileWriter;
import com.hackorama.plethora.server.io.SecurityManager;
import com.hackorama.plethora.server.jmx.PlethoraServerMBean;
import com.hackorama.plethora.server.jmx.ServerJMXAgent;
import com.hackorama.plethora.server.snmp.SnmpAgent;
import com.hackorama.plethora.server.web.WebHandler;
import com.hackorama.plethora.server.web.WebServer;
import com.hackorama.plethora.server.web.handler.DynamicPageHandler;
import com.hackorama.plethora.server.web.handler.Formatters;
import com.hackorama.plethora.server.web.handler.StaticPageHandler;

public class Controller {

    private final String name;
    private final ServerConfiguration configuration;
    private ScheduledExecutorService metricExecutor;
    private ScheduledExecutorService moduleExecutor;
    private ScheduledFuture<?> connectionService;
    private ScheduledFuture<?> dataService;
    private List<ScheduledFuture<?>> dataServices;
    private MetricService metricService;
    private WebServer webServer;
    private ServerJMXAgent jmxAgent;
    private SnmpAgent snmpAgent;
    private SecureFileWriter secureFileWriter;
    private SecureFileReader secureFileReader;

    public Controller(ServerConfiguration configuration) {
        this(configuration, null);
    }

    public Controller(ServerConfiguration configuration, String name) {
        if (configuration == null) {
            throw new IllegalArgumentException("Valid configuration is required");
        }
        this.configuration = configuration;
        this.name = name == null ? "Plethora Service Controller" : name;
        initSecurityPermissions();
        initCommonServices();
    }

    public ScheduledFuture<?> startService(ScheduledExecutorService executor, String serviceName, Callable<?> task,
            long delaySeconds) {
        ScheduledFuture<?> service = executor.scheduleAtFixedRate(new TaskThread(serviceName, task, Log.getLogger()),
                delaySeconds, delaySeconds, TimeUnit.SECONDS);
        Log.getLogger()
                .info(name + " : " + serviceName + " service scheduled at " + delaySeconds + " second intervals");
        return service;
    }

    public void startModuleConnectionService() {
        moduleExecutor = TaskExecutors.newScheduledService("ModuleService");
        connectionService = startService(moduleExecutor, "connection manager", new Callable<Boolean>() {
            @Override
            final public Boolean call() {
                return executeConnectionTask();
            }
        }, configuration.getModuleConnectionRetryInterval());
    }

    public void startDataService() {
        metricExecutor = TaskExecutors.newScheduledService("Metricervice");
        dataService = startService(metricExecutor, "data refresh", new Callable<Boolean>() {
            @Override
            final public Boolean call() {
                return executeDataTask();
            }
        }, configuration.getDataRefreshSeconds());
    }

    public void startDataServices() {
        dataServices = new ArrayList<ScheduledFuture<?>>();
        metricExecutor = TaskExecutors.newScheduledService("MetricService", metricService.getModuleCount());
        for (final String module : metricService.getModuleList()) {
            ScheduledFuture<?> service = startService(metricExecutor, "data refresh", new Callable<Boolean>() {
                @Override
                final public Boolean call() {
                    return executeDataTask(module);
                }
            }, configuration.getDataRefreshSeconds());
            dataServices.add(service);
        }
    }

    public void cancelDataService() {
        dataService.cancel(true);
        Log.getLogger().finest(name + " : data service cancelled");
    }

    public void cancelDataServices() {
        for (ScheduledFuture<?> service : dataServices) {
            service.cancel(true);
        }
        Log.getLogger().finest(name + " : data services cancelled");
    }

    public void cancelConnectionService() {
        connectionService.cancel(true);
        Log.getLogger().finest(name + " : connection service cancelled");
    }

    public void startWebService() {
        Executor executor = TaskExecutors.newService("HTTPServer", configuration.getHttpThreadPoolSize());
        WebHandler handler = new WebHandler(new DynamicPageHandler(metricService, new Formatters()),
                new StaticPageHandler(configuration.getWebRoot(), secureFileReader));
        webServer = new WebServer(configuration.getHttpHostname(), configuration.getHttpPort(), handler, executor);
        startServer(webServer);
        Log.getLogger().finest(name + " : web server started");
    }

    public void stopWebService() {
        startServer(webServer);
        Log.getLogger().finest(name + " : web server stopped");
    }

    public void startJMXService() {
        jmxAgent = new ServerJMXAgent("plethora", new PlethoraServerMBean(metricService),
                configuration.getJmxHostname(), configuration.getJmxPort());
        jmxAgent.setUpdateMode(configuration.isJmxMBeanForceUpdated());
        startServer(jmxAgent);
        Log.getLogger().finest(name + " : jmx server started");
    }

    public void stopJMXService() {
        stopServer(jmxAgent);
    }

    public void startSNMPService() {
        Executor executor = TaskExecutors.newService("SNMPServer", configuration.getSnmpThreadPoolSize());
        snmpAgent = new SnmpAgent(configuration, metricService, secureFileWriter, executor);
        startServer(snmpAgent);
        Log.getLogger().finest(name + " : snmp server started");
    }

    public void stopSNMPService() {
        stopServer(snmpAgent);
    }

    public void initModules() {
        metricService.initModules();
        Log.getLogger().finest(name + " : moduels initialized");
    }

    public void reportStatus() {
        final String EOL = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder();
        result.append(EOL + "Active task thread groups");
        for (Entry<String, String> entry : TaskFactory.threadGroupInfo().entrySet()) {
            result.append(EOL + entry.getKey() + "\t" + entry.getValue());
        }
        Map<Long, Thread> allThreads = new HashMap<Long, Thread>();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            allThreads.put(thread.getId(), thread);
        }
        result.append(EOL + EOL + "Active task threads");
        for (Long id : TaskFactory.threadIds()) {
            Thread thread = allThreads.get(id);
            result.append(EOL + id + " " + thread.getName() + "\t" + thread.getState().name());
        }
        Log.getLogger().info("Service Status\n" + result.toString() + EOL + EOL + "Plethora Server Ready" + EOL);
    }

    private void initSecurityPermissions() {
        try {
            String webRoot = configuration.getWebRoot();
            String snmpMibFile = webRoot + File.separatorChar + configuration.getSnmpMibFilePath();
            SecurityManager.getInstance().getFileAccessPermission().addReadPermittedDirectory(webRoot);
            SecurityManager.getInstance().getFileAccessPermission().addWritePermittedFile(snmpMibFile);
        } catch (IOException e) {
            throw new SecurityException("The secure file access is not initailized correctly", e);
        }
    }

    private final void initCommonServices() {
        Log.initLogger(configuration);
        metricService = new MetricService(new Modules(configuration));
        if (configuration.isHighPrecisionMode()) {
            metricService.enableImmediateMode();
        }
        secureFileWriter = new SecureFileWriter(SecurityManager.getInstance().getFileAccessPermission());
        secureFileReader = new SecureFileReader(SecurityManager.getInstance().getFileAccessPermission());
    }

    private Boolean executeConnectionTask() {
        Log.getLogger().finest(name + " : connection service retrying connection ... ");
        metricService.pollModules();
        return true;
    }

    private Boolean executeDataTask() {
        Log.getLogger().finest(name + " : data refresh service updating all metrics ... ");
        metricService.refreshMetrics();
        return true;
    }

    private Boolean executeDataTask(String module) {
        Log.getLogger().finest(name + " : data refresh service updating " + module + "  metrics ...");
        metricService.refreshMetrics(module);
        return true;
    }

    private void startServer(MetricServer server) {
        if (server instanceof Callable<?>) {
            try {
                ((Callable<?>) server).call();
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE, "Server failure " + server.getName(), e);
            }
            metricService.registerServer(server);
        } else {
            Log.getLogger().severe(
                    "Cannot start server " + server.getName() + ", does not implement required Callable interface");
        }
    }

    private void stopServer(MetricServer metricServer) {
        if (metricServer != null) {
            metricServer.stop();
        }
    }

}
