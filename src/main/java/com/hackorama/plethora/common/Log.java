package com.hackorama.plethora.common;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Log {

    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    public static final int DEFAULT_LOG_COUNT = 10;
    public static final int DEFAULT_LOG_LIMIT = 10485760; // bytes

    private static final String PROPERTY_LOG_ROOT = "log.root";
    private static final String PROPERTY_LOG_FILE = "log.file";
    private static final String PROPERTY_LOG_LIMIT = "log.limit";
    private static final String PROPERTY_LOG_COUNT = "log.count";
    private static final String PROPERTY_LOG_LEVEL = "log.level";
    private static final String LOG_FILE_NAME = "server.log.%g";

    private static FileHandler fileHandler;
    private static Configuration configuration;

    // logger is not final, to use application provided logger at run time
    private static Logger logger = Logger.getLogger("com.hackorama.plethora");

    private Log() {
        // no instances
    }

    public static void initLogger(Configuration configuration) {
        Log.configuration = configuration;
        initLogger(getFile(), getLimit(), getCount(), getLevel());
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        if (logger == null) {
            Log.logger.warning("No valid logger provided, logs will be printed to the console stderr");
        } else {
            Log.logger = logger;
        }
    }

    /*
     * Thread safe single logger initialization
     * 
     * NOTE: Synchronization used only during initialization, so no block level synchronization optimization
     */
    private static synchronized void initLogger(String logFile, int limit, int count, String level) {
        logger.setLevel(parseLevel(level));
        logger.info("Log level " + logger.getLevel());
        if (fileHandler != null) {
            logger.warning("Logger already initialized");
            return;
        }
        String fileName = logFile != null ? logFile : getLogRoot() + File.separatorChar + LOG_FILE_NAME;
        try {
            fileHandler = new FileHandler(fileName, limit, count, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (SecurityException e) {
            logger.severe("Log handler failed with security exception : " + e.getMessage());
            logger.severe("Plethora server cannot log to file " + fileName + ", please verify file permissions ");
            logger.severe("Logs will be printed to the console stderr");
        } catch (IOException e) {
            logger.severe("Log handler failed with IO exception : " + e.getMessage());
            logger.severe("Plethora server cannot log to file " + fileName + ", please verify file permissions ");
            logger.severe("Logs will be printed to the console stderr");
        }
    }

    private static Level parseLevel(String name) {
        if (name == null) {
            return DEFAULT_LOG_LEVEL;
        }
        try {
            return Level.parse(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_LOG_LEVEL;
        }
    }

    private static String getLogRoot() {
        return configuration.getProperty(PROPERTY_LOG_ROOT, "");
    }

    private static String getFile() {
        return configuration.getProperty(PROPERTY_LOG_FILE);
    }

    private static int getLimit() {
        return Util.getInt(configuration.getProperty(PROPERTY_LOG_LIMIT), DEFAULT_LOG_LIMIT);
    }

    private static int getCount() {
        return Util.getInt(configuration.getProperty(PROPERTY_LOG_COUNT), DEFAULT_LOG_COUNT);
    }

    private static String getLevel() {
        return configuration.getProperty(PROPERTY_LOG_LEVEL);
    }
}
