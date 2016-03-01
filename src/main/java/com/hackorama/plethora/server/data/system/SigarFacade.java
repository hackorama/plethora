package com.hackorama.plethora.server.data.system;

import java.util.HashMap;
import java.util.Map;

import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.hyperic.sigar.ptql.ProcessFinder;

/**
 * Facade to the system access provided by SIGAR
 * 
 * http://www.hyperic.com/support/docs/sigar/
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class SigarFacade implements SystemAccess {

    static private abstract class MetricCallWrapper {
        public abstract long get(long pid) throws SystemAccessException;
    }

    private static final String LIBRARY_NAME = "SIGAR";
    private Sigar sigar;
    private final SigarProxy sigarProxy;

    private ProcessFinder processFinder;

    private final Map<SYSTEM_METRIC_TYPE, MetricCallWrapper> metricCallables = new HashMap<SYSTEM_METRIC_TYPE, MetricCallWrapper>();

    public SigarFacade() {
        init();
        sigarProxy = SigarProxyCache.newInstance(sigar);
    }

    public SigarFacade(long cacheExpirySecs) {
        init();
        sigarProxy = SigarProxyCache.newInstance(sigar, (int) cacheExpirySecs * 1000);
    }

    @Override
    public long findPid(String query) throws SystemAccessException {
        long[] pids;
        try {
            pids = processFinder.find(query);
        } catch (SigarException e) {
            throw new SystemAccessException(e);
        }
        return pids.length > 0 ? pids[0] : 0;
    }

    @Override
    public final long getCpuUsage() throws SystemAccessException {
        try {
            return Math.round(sigarProxy.getCpuPerc().getCombined() * 100);
        } catch (SigarException e) {
            throw new SystemAccessException(e);
        }
    }

    @Override
    public final long getCpuUsage(long pid) throws SystemAccessException {
        try {
            return Math.round(sigarProxy.getProcCpu(pid).getPercent() * 100);
        } catch (SigarException e) {
            throw new SystemAccessException(e);
        }
    }

    @Override
    public final long getMemUsage() throws SystemAccessException {
        try {
            return Math.round(sigarProxy.getMem().getUsedPercent());
        } catch (SigarException e) {
            throw new SystemAccessException(e);
        }
    }

    @Override
    public final long getMemUsage(long pid) throws SystemAccessException {
        try {
            long totalMem = sigarProxy.getMem().getTotal();
            long usedMem = sigarProxy.getProcMem(pid).getResident();
            return perCent(totalMem, usedMem);
        } catch (SigarException e) {
            throw new SystemAccessException(e);
        }
    }

    @Override
    public String getName() {
        return LIBRARY_NAME;
    }

    private ProcState getProcState(long pid) {
        if (pid > 0) {
            try {
                return sigarProxy.getProcState(pid);
            } catch (SigarException ignoreException) {
                // any sigar error count as non available process
            }
        }
        return null;
    }

    @Override
    public long getMetric(SYSTEM_METRIC_TYPE type) throws SystemAccessException {
        return getMetric(type, 0);
    }

    @Override
    public long getMetric(SYSTEM_METRIC_TYPE type, long pid) throws SystemAccessException {
        MetricCallWrapper metricCallable = metricCallables.get(type);
        if (metricCallable == null) {
            throw new SystemAccessException("Unknwon metric type " + type);
        }
        return metricCallable.get(pid);
    }

    private void init() {
        sigar = new Sigar();
        processFinder = new ProcessFinder(sigar);
        initSysMetricCallables();
    }

    private void initSysMetricCallables() {
        metricCallables.put(SYSTEM_METRIC_TYPE.cpu, new MetricCallWrapper() {
            @Override
            public long get(long pid) throws SystemAccessException {
                return pid > 0 ? getCpuUsage(pid) : getCpuUsage();
            }
        });
        metricCallables.put(SYSTEM_METRIC_TYPE.memory, new MetricCallWrapper() {
            @Override
            public long get(long pid) throws SystemAccessException {
                return pid > 0 ? getMemUsage(pid) : getMemUsage();
            }
        });

    }

    @Override
    public boolean isAvaialbleProcess(long pid) {
        return pid > 0 ? getProcState(pid) != null : false;
    }

    @Override
    public boolean isAvailable() {
        // To validate SIGAR is available, check successful execution of a SIGAR
        // API call, without any SigarException or runtime exceptions thrown.
        try {
            return sigarProxy != null ? sigarProxy.getPid() > 0 : false;
        } catch (Exception ignoringAllExceptions) {
            // catch all exception by intent, validating SIGAR api call
            return false;
        }
    }

    @Override
    public boolean isRunningProcess(long pid) {
        if (pid > 0) {
            ProcState procState = getProcState(pid);
            if (procState != null) {
                char state = procState.getState();
                return state == ProcState.IDLE || state == ProcState.RUN || state == ProcState.SLEEP;
            }
        }
        return false;
    }

    private long perCent(double total, double value) {
        return total > 0 ? Math.round(value / total * 100) : 0;
    }

}
