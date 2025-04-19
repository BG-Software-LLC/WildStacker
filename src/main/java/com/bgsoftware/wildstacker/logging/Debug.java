package com.bgsoftware.wildstacker.logging;

import java.util.Locale;

public enum Debug {

    PROFILER,
    DATABASE_QUERY,
    SHOW_STACKTRACE;

    private static String[] DEBUG_NAMES = null;

    public static String[] getDebugNames() {
        if (DEBUG_NAMES == null) {
            Debug[] debugs = Debug.values();
            DEBUG_NAMES = new String[debugs.length];
            for (int i = 0; i < debugs.length; ++i)
                DEBUG_NAMES[i] = debugs[i].name().toLowerCase(Locale.ENGLISH);
        }

        return DEBUG_NAMES;
    }
}
