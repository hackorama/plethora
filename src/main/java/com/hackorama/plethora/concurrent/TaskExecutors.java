package com.hackorama.plethora.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Task executor services utility methods thats uses {@link TaskFactory}
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class TaskExecutors {

    public static ScheduledExecutorService newScheduledService(String name) {
        return newScheduledService(name, null);
    }

    public static ScheduledExecutorService newScheduledService(String name, String info) {
        return Executors.newSingleThreadScheduledExecutor(new TaskFactory(name, info));
    }

    public static ScheduledExecutorService newScheduledService(String name, int poolSize) {
        return newScheduledService(name, poolSize, null);
    }

    public static ScheduledExecutorService newScheduledService(String name, int poolSize, String info) {
        return Executors.newScheduledThreadPool(poolSize, new TaskFactory(name, info));
    }

    public static ExecutorService newService(String name) {
        return newService(name, null);
    }

    public static ExecutorService newService(String name, String info) {
        return Executors.newSingleThreadExecutor(new TaskFactory(name, info));
    }

    public static ExecutorService newService(String name, int poolSize) {
        return newService(name, poolSize, null);
    }

    public static ExecutorService newService(String name, int poolSize, String info) {
        return Executors.newFixedThreadPool(poolSize, new TaskFactory(name, info));
    }

    private TaskExecutors() {
        // no instances
        throw new AssertionError();
    }
}
