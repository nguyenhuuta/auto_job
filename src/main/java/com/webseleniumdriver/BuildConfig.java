package com.webseleniumdriver;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;

public class BuildConfig {

    public static final String PROJECT = "WebSeleniumDriver";
    public static final String VERSION_NAME = "3.1.6";

    public static final String PLATFORM = SystemUtils.OS_NAME;

    public static final String PATH_FILE = Paths.get("").toAbsolutePath().toString();

    private BuildConfig() {
        //no instance
    }

    public static String getInfoVersion() {
        return "OS: " + PLATFORM + " - " + "Version: " + VERSION_NAME + " - Path:" + PATH_FILE;
    }
}
