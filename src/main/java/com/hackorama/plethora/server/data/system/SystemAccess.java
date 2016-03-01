package com.hackorama.plethora.server.data.system;

/**
 * The system level access implemented by a system access module
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public interface SystemAccess {
    long findPid(String query) throws SystemAccessException;

    long getCpuUsage() throws SystemAccessException;

    long getCpuUsage(long pid) throws SystemAccessException;

    long getMemUsage() throws SystemAccessException;

    long getMemUsage(long pid) throws SystemAccessException;

    long getMetric(SYSTEM_METRIC_TYPE type, long pid) throws SystemAccessException;

    long getMetric(SYSTEM_METRIC_TYPE type) throws SystemAccessException;

    String getName();

    boolean isAvailable();

    boolean isAvaialbleProcess(long pid);

    boolean isRunningProcess(long pid);
}
