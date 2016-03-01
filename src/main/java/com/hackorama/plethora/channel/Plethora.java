package com.hackorama.plethora.channel;

import java.util.logging.Logger;

public final class Plethora {

    private static Channel channelPlethora = null;

    /*
     * Thread safe initialization
     * 
     * NOTE: Synchronization used only during initialization, so no block level synchronization optimization
     */
    public static synchronized boolean initPlethora(String name, String configfile, Logger logger) {
        if (channelPlethora == null) {
            channelPlethora = new com.hackorama.plethora.channel.Channel(name, configfile, logger);
        }
        return channelPlethora.launch();
    }

    public static Object getMetric(String metric) {
        return channelPlethora.getMetric(metric);
    }

    public static Long getNumberMetric(String metric) {
        return channelPlethora.getNumberMetric(metric);
    }

    public static String getTextMetric(String metric) {
        return channelPlethora.getTextMetric(metric);
    }

    public static Boolean getBooleanMetric(String metric) {
        return channelPlethora.getBooleanMetric(metric);
    }

    public static boolean setMetric(String metric, long value) {
        return channelPlethora == null ? false : channelPlethora.setMetric(metric, value);
    }

    public static boolean setMetric(String metric, String value) {
        return channelPlethora == null ? false : channelPlethora.setMetric(metric, value);
    }

    public static boolean setMetric(String metric, boolean value) {
        return channelPlethora == null ? false : channelPlethora.setMetric(metric, value);
    }

    public static boolean incrMetric(String metric) {
        return channelPlethora == null ? false : channelPlethora.incrMetric(metric);
    }

    public static boolean incrMetric(String metric, long delta) {
        return channelPlethora == null ? false : channelPlethora.incrMetric(metric, delta);
    }

    public static boolean decrMetric(String metric) {
        return channelPlethora == null ? false : channelPlethora.decrMetric(metric);
    }

    public static boolean decrMetric(String metric, long delta) {
        return channelPlethora == null ? false : channelPlethora.decrMetric(metric, delta);
    }

    // do not allow instance creation
    private Plethora() {
        // not even from within the class
        throw new AssertionError();
    }
}
