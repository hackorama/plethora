package com.hackorama.plethora.examples;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

// STEP 1. Import client packages
import com.hackorama.plethora.channel.Plethora;

/**
 * Example of exposing metrics from an application using Plethora client library
 * in three quick steps
 * 
 * @author Kishan Thomas <kishan@hackorama.com>
 *
 */
public class DemoAppServer {

    private static final Logger LOGGER = Logger.getLogger(DemoAppServer.class.getName());

    private static long getCount() {
        return new Random().nextInt(10);
    }

    private static long getRate() {
        return new Random().nextInt(100);
    }

    public static void main(String[] args) {

        LOGGER.setLevel(Level.INFO);
        String configFile = args.length > 0 ? args[0] : "src/test/resources/examples/jdemo.metrics.properties";

        // STEP 2. Initialize metrics defined in the property file
        Plethora.initPlethora("jdemo", configFile, LOGGER);

        // STEP 3. Set metrics
        Plethora.setMetric("cache_mode", "disabled");
        Plethora.setMetric("log_level", LOGGER.getLevel().toString());

        while (true) {
            runServer();
        }
    }

    private static void runServer() {
        // STEP 3. Set metrics
        Plethora.setMetric("connection", getCount());
        Plethora.setMetric("queue_fill_rate", getRate());
        waitSeconds(10);
    }

    private static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
