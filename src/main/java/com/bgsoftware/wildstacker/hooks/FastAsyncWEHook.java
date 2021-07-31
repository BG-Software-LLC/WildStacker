package com.bgsoftware.wildstacker.hooks;

import com.boydti.fawe.config.Settings;

public final class FastAsyncWEHook {

    public static void disableTicksLimiter() {
        Settings.IMP.TICK_LIMITER.ITEMS = Integer.MAX_VALUE;
    }

}
