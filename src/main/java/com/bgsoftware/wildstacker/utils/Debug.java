package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;

import java.util.logging.Level;

public class Debug {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private Debug() {

    }

    public static void debug(String clazz, String method, String message) {
        plugin.getLogger().log(Level.INFO, clazz + "::" + method + " " + message);
    }

}
