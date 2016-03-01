package com.hackorama.plethora.server.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.ThreadFactory;
import org.snmp4j.util.WorkerTask;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.config.SnmpConfiguration;
import com.hackorama.plethora.server.MetricServer;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.io.SecureFileWriter;

public final class SnmpAgent implements CommandResponder, MetricServer, Callable<Object> {

    private static final String NAME = "SNMP agent server";

    private final String host;
    private final int port;
    private final Snmp snmp;
    private final OID rootOid;
    private final OID rootOidScalar; // TODO remove if no client requests found
    private final String oidNumbers;
    private final String oidNames;
    private final SystemGroup systemGroup;
    private final OidToMetricBridge oidToMetricBridge;
    private final MIBEngine mibEngine;
    private final String sysDescr;
    private final Executor executor;

    public SnmpAgent(SnmpConfiguration configuration, MetricService metricService, SecureFileWriter secureFileWriter) {
        this(configuration, metricService, secureFileWriter, null);
    }

    public SnmpAgent(SnmpConfiguration configuration, MetricService metricService, SecureFileWriter secureFileWriter,
            Executor executor) {
        host = configuration.getSnmpHostname();
        port = configuration.getSnmpPort();
        oidNumbers = configuration.getSnmpEnterpriseSubTreeNumbers();
        if (!Util.validPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        if (oidNumbers == null) {
            throw new IllegalArgumentException("OID can't be null");
        }
        // oidNames used in mib gen and can have any value including null
        oidNames = configuration.getSnmpEnterpriseSubTreeNames();
        rootOid = new OID(oidNumbers).append(".1"); // metrics sub tree
        String mibUrl = configuration.getSnmpMibUrl();
        mibEngine = new MIBEngine(metricService, secureFileWriter, mibUrl, configuration.getWebRoot(), configuration
                .getMetaConfiguration().getProductName());
        rootOidScalar = new OID(rootOid).append(".0");
        systemGroup = new SystemGroup(configuration, rootOid, mibUrl);
        sysDescr = configuration.getSnmpSysDescr();
        oidToMetricBridge = new OidToMetricBridge(rootOid, metricService);
        this.executor = executor;
        try {
            snmp = initSnmp(getUdpAddress(host, port));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Please provide valid hostname and port", e);
        }
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        if (event == null || event.getPDU() == null) {
            Log.getLogger().warning("Invalid SNMP event or PDU");
            return;
        }
        PDU eventPdu = event.getPDU();
        PDU responsePdu = new PDU(eventPdu);
        responsePdu.setType(PDU.RESPONSE);
        switch (eventPdu.getType()) {
        case PDU.GET:
            handleSnmpGet(responsePdu);
            break;
        case PDU.GETNEXT:
            handleSnmpGetnext(responsePdu);
            break;
        case PDU.GETBULK:
            handleSnmpGetbulk(responsePdu);
            break;
        default:
            Log.getLogger().info("SNMP event type" + eventPdu.getType() + " not supported by plethora");
            return;
        }
        eventDispatch(event, responsePdu);
    }

    @Override
    public boolean start() {
        String msg = NAME + " at " + host + ":" + port;
        if (snmp == null) {
            Log.getLogger().info("Failed to start " + msg + ", SNMP not initialised");
            return false;
        }
        snmp.addCommandResponder(this);
        try {
            snmp.listen();
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "Failed to start " + msg + ", IO error", e);
            return false;
        }
        mibEngine.generateMib(oidNumbers, oidNames);
        Log.getLogger().info("Started  " + msg);
        return true;
    }

    @Override
    public void stop() {
        if (snmp != null) {
            try {
                snmp.close();
                Log.getLogger().info("Stopped " + NAME);
            } catch (IOException e) {
                Log.getLogger().log(Level.SEVERE, "Failed to stop " + NAME + ", IO error", e);
            }
        }
    }

    @Override
    public Object call() throws Exception {
        return start();
    }

    @Override
    public boolean updateNotify() {
        Log.getLogger().info("Updating " + NAME);
        return mibEngine.generateMib(oidNumbers, oidNames);
    }

    @Override
    public String getName() {
        return "SNMP agent " + rootOid.toString();
    }

    private void eventDispatch(CommandResponderEvent event, PDU responsePdu) {
        event.getStateReference().setTransportMapping(event.getTransportMapping());
        try {
            event.getMessageDispatcher().returnResponsePdu(event.getMessageProcessingModel(), event.getSecurityModel(),
                    event.getSecurityName(), event.getSecurityLevel(), responsePdu, event.getMaxSizeResponsePDU(),
                    event.getStateReference(), new StatusInformation());
        } catch (MessageException e) {
            Log.getLogger().log(Level.SEVERE, "Error processing PDU", e);
        }
    }

    private Snmp initSnmp(UdpAddress udpAddress) {
        if (executor != null) {
            SNMP4JSettings.setThreadFactory(new WrappedThreadFactory(executor));
        }
        try {
            return new Snmp(new DefaultUdpTransportMapping(udpAddress));
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "Could not initialize SNMP", e);
        }
        return null;
    }

    private void handleSnmpGet(PDU responsePdu) {
        for (VariableBinding binding : responsePdu.toArray()) {
            OID oid = binding.getOid();
            Variable variable = getValueforOID(oid);
            binding.setVariable(variable);
            Log.getLogger().fine("SNMP Get " + oid.toString() + " = " + variable);
        }
    }

    private void handleSnmpGetnext(PDU responsePdu) {
        // TODO processing only the first binding
        // investigate case of processing all from PDU.toArray()
        if (responsePdu.size() < 1) {
            Log.getLogger().warning("SNMP GetNext no variable binding");
            return;
        }
        VariableBinding binding = responsePdu.get(0);
        OID oid = binding.getOid();
        OidValue nextOidValue = getNextOidValue(oid);
        if (nextOidValue == null) {
            binding.setOid(new OID());
            // Sending back SNMP_ERROR_GENERAL_ERROR caused client error: 
            //    Error in packet. Reason: (genError) A general failure occured
            responsePdu.setErrorStatus(SnmpConstants.SNMP_ERROR_SUCCESS);
            responsePdu.setErrorIndex(1);
            binding.setVariable(Null.endOfMibView);
            Log.getLogger().fine("SNMP GetNext endOfMibView");
        } else {
            binding.setOid(nextOidValue.getOid());
            binding.setVariable(nextOidValue.getValue());
            Log.getLogger().fine("SNMP GetNext " + nextOidValue.getOid() + " = " + nextOidValue.getValue());
        }
    }

    private void handleSnmpGetbulk(PDU responsePdu) {
        Log.getLogger().warning("SNMP GetBulk Not Imeplemented");
    }

    private OidValue getNextOidValue(OID oid) {
        int sysOidSize = rootOid.size();
        if (oid.startsWith(rootOid)) { // 1. metrics MIB tree
            int[] oidArray = oid.toIntArray();
            int moduleIndex = 0, metricIndex = 0;
            if (oid.size() == sysOidSize + 1) {
                // module only, no metric
                moduleIndex = oidArray[sysOidSize];
            } else if (oid.size() == sysOidSize + 2) {
                // module and metric
                moduleIndex = oidArray[sysOidSize];
                metricIndex = oidArray[sysOidSize + 1];
            }
            return oidToMetricBridge.getNextMetricOidValue(moduleIndex, metricIndex);
        } else if (systemGroup.hasNext(oid)) { // 2. MIB-2 system
            return new OidValue(systemGroup.getNextSysOid(oid), systemGroup.getSysOidVariable(oid));
        }
        // 3. with invalid (zero) start MIB walk at top of MIB tree
        return oidToMetricBridge.getFirstMetricOidValue();
    }

    private Variable getValueforOID(OID oid) {
        // NOTE: Not checking for the '0' append for scalar variable OID's,
        // since we are not using any table variable OID in the MIB.
        if (oid.startsWith(rootOid)) {
            if (rootOid.equals(oid) || rootOidScalar.equals(oid)) {
                return new OctetString(sysDescr);
            }
            return oidToMetricBridge.getMetricOidVariable(oid);
        } else if (systemGroup.isSysOID(oid)) {
            return systemGroup.getSysOidVariable(oid);
        }
        Log.getLogger().warning("The oid " + oid + " is not a mapped one");
        return Null.noSuchObject;
    }

    private UdpAddress getUdpAddress(String host, int port) throws UnknownHostException {
        return new UdpAddress(InetAddress.getByName(host), port);
    }

    private static class WrappedThreadFactory implements ThreadFactory {

        private final Executor executor;

        public WrappedThreadFactory(Executor executor) {
            this.executor = executor;
        }

        @Override
        public WorkerTask createWorkerThread(String name, WorkerTask task, boolean daemon) {
            // TODO handling daemon task
            return new ExecutorWorkerTask(executor, task);
        }
    }

    private static class ExecutorWorkerTask implements WorkerTask {
        private final WorkerTask workerTask;
        private final Executor executor;

        public ExecutorWorkerTask(Executor executor, WorkerTask workerTask) {
            this.workerTask = workerTask;
            this.executor = executor;
        }

        @Override
        public void run() {
            executor.execute(workerTask);
        }

        @Override
        public void interrupt() {
            workerTask.interrupt();
        }

        @Override
        public void join() throws InterruptedException {
            workerTask.join();
        }

        @Override
        public void terminate() {
            workerTask.terminate();
        }
    }
}
