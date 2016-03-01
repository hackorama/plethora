package com.hackorama.plethora.concurrent;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task thread wrapper for Callable tasks with exception logging
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class TaskThread implements Runnable {
    private final Callable<?> task;
    private final String name;
    private final Logger logger;

    public TaskThread(String name, Callable<?> task, Logger logger) {
        this.name = name;
        this.task = task;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            task.call();
        } catch (Exception e) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Task " + name + " failed", e);
            }
        }
    }
}
