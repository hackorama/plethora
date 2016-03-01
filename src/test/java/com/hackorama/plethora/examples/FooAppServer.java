package com.hackorama.plethora.examples;

import java.util.Random;
import java.util.logging.Logger;

import com.hackorama.plethora.channel.Plethora;

public class FooAppServer {

    private static final Logger LOGGER = Logger.getLogger(FooAppServer.class.getName());

    public static void main(String[] args) {

        Plethora.initPlethora("foo", "examples/fooserver.prperties", LOGGER);

        Plethora.setMetric("cache_mode", "disabled");
        Plethora.setMetric("log_level", LOGGER.getLevel().getName());

        while (true) {
            runServer();
        }
    }

    private static void waitSeconds(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void runServer() {

        Plethora.setMetric("connection_count", getCount());
        Plethora.setMetric("transaction_rate", getRate());

        waitSeconds(10);
    }

    private static long getRate() {
        Random random = new Random();
        return random.nextInt(100);
    }

    private static long getCount() {
        Random random = new Random();
        return random.nextInt(10);
    }

}
