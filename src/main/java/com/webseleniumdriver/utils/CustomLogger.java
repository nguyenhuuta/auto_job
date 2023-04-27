package com.webseleniumdriver.utils;


import org.apache.log4j.Logger;

public class CustomLogger {
    private String logPrefix;
    private Logger logger;
    private int logLevel = LEVEL_DEBUG;

    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;

    public CustomLogger(String className) {
        logPrefix = className + ": ";
        logger = Logger.getLogger(className);
    }
    
    public void info(String message) {
        if (logLevel <= LEVEL_INFO) {
            logger.info(logPrefix + message);
        }
    }

    public void info(String message, Throwable throwable) {
        if (logLevel <= LEVEL_INFO) {
            logger.info(logPrefix + message, throwable);
        }
    }

    public void error(Object object) {
        if (logLevel <= LEVEL_INFO) {
            logger.error(logPrefix + (object != null ? object.toString() : "null"));
        }
    }

    public void error(String message) {
        if (logLevel <= LEVEL_INFO) {
            logger.error(logPrefix + message);
        }
    }

    public void error(String message, Throwable throwable) {
        if (logLevel <= LEVEL_INFO) {
            logger.error(logPrefix + message, throwable);
        }
    }
}
