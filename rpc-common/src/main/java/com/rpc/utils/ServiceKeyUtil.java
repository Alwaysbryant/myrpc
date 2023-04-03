package com.rpc.utils;

public class ServiceKeyUtil {
    public static final String CHAR = "#";

    public static String generateKey(String name, String version) {
        String key = name;
        if (version != null && version.trim().length() > 0) {
            key += CHAR + version;
        }
        return key;
    }
}
