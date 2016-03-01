package com.hackorama.plethora.concurrent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom ThreadFactory implementation for Plethora tasks
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class TaskFactory implements java.util.concurrent.ThreadFactory {
    private static final String PREFIX = "Plethora";
    private static final Map<Long, String> THREAD_INFO = new LinkedHashMap<Long, String>();
    private static final Map<String, String> GROUP_INFO = new LinkedHashMap<String, String>();
    private static AtomicLong groupCounter = new AtomicLong(1);
    private final AtomicLong threadCounter = new AtomicLong(1);
    private String groupName;
    private String description;

    public TaskFactory() {
        groupCounter.getAndIncrement();
    }

    public TaskFactory(String groupName, String description) {
        this();
        this.groupName = groupName != null ? groupName : "";
        this.description = description != null ? description : "";
        GROUP_INFO.put(buildGroupName(), this.description);
    }

    public TaskFactory(String groupName) {
        this(groupName, null);
    }

    private String buildGroupName() {
        return PREFIX + groupName + "-" + groupCounter;
    }

    private String buildThreadName() {
        return buildGroupName() + "-" + threadCounter.getAndIncrement();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = buildThreadName();
        Thread thread = new Thread(runnable, name);
        THREAD_INFO.put(thread.getId(), name);
        return thread;
    }

    public static Map<Long, String> threadInfo() {
        return THREAD_INFO;
    }

    public static Map<String, String> threadGroupInfo() {
        return GROUP_INFO;
    }

    public static Set<Long> threadIds() {
        return THREAD_INFO.keySet();
    }

}
