package com.hackorama.plethora.server.data.jmx;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;

final class ConnectionThreadTracker {

    private static final String JMX_JOB_EXE_THREAD = "com.sun.jmx.remote.opt.util.JobExecutor";

    private ConnectionThreadTracker() {
        // no instances
    }

    static void reclaimWaiting() {
        reclaimWaiting(0);
    }

    /*
     * All thread state checks and changes need to be done one at a time, all thread operations in this method and child
     * methods are synchronized
     */
    static synchronized void reclaimWaiting(int allowedWaitSecs) {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }
        Thread[] threads = new Thread[rootGroup.activeCount()];
        while (rootGroup.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        for (Thread thread : threads) {
            if (null != thread && JMX_JOB_EXE_THREAD.equals(thread.getClass().getName())) {
                try {
                    forceThreadStop(thread, allowedWaitSecs);
                } catch (Exception e) {
                    // do not let any errors during thread access to propagate
                    Log.getLogger().log(Level.SEVERE, "Recliamiming jmx connection thread failed", e);
                }
            }
        }
    }

    // suppress thread.stop() deprecation
    @SuppressWarnings("deprecation")
    private static void forceThreadStop(Thread thread, int allowedWaitSecs) throws Exception {
        debugThread(thread);
        String name = thread.getName();
        long threadId = thread.getId();
        // check if the thread is within allowed wait grace period
        if (thread.getState() == State.WAITING && allowedWaitSecs > 0) {
            long waitingTimeSecs = trackedWaitingTimeSecs(threadId);
            if (trackedWaitingTimeSecs(threadId) < allowedWaitSecs) {
                Log.getLogger().fine("Allowing " + name + " " + threadId + " waiting for " + waitingTimeSecs + " secs");
                return;
            }
        }
        if (thread.getState() == State.WAITING) {
            Log.getLogger().fine("Interruping " + name + " " + threadId);
            // 1. try interrupting thread as the first choice
            thread.interrupt();
        }
        // 2. if the thread is still around
        if (thread.getState() == State.WAITING) {
            Log.getLogger().fine("Stopping " + name + " " + threadId);
            // 3. no other choice to gracefully close the zombie jmx threads
            thread.stop();
        }
        resetTrackedWaitingTime(threadId);
    }

    private static final Map<Long, Long> TIME_KEEPER = new HashMap<Long, Long>();

    private static long trackedWaitingTimeSecs(Long threadId) {
        Long now = System.currentTimeMillis();
        if (TIME_KEEPER.containsKey(threadId)) {
            return (now - TIME_KEEPER.get(threadId)) / 1000;
        } else {
            TIME_KEEPER.put(threadId, System.currentTimeMillis());
            return 0;
        }
    }

    private static void resetTrackedWaitingTime(long threadId) {
        TIME_KEEPER.remove(threadId);
    }

    private static void debugThread(Thread thread) {
        Log.getLogger().fine(
                thread.getId() + " " + thread.getName() + " " + thread.getState() + " a=" + thread.isAlive() + " d="
                        + thread.isDaemon() + " i=" + thread.isInterrupted() + " " + thread.getClass());
    }
}
