package com.bgsoftware.wildstacker.hooks;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.inventory.ItemStack;

public final class SlimefunHook {

    private static boolean enabled = false;

    public static void setEnabled(boolean enabled) {
        if (enabled) {
            try {
                Class.forName("me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem");
                SlimefunHook.enabled = true;
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }
        SlimefunHook.enabled = false;
    }

    public static boolean isSlimefunItem(ItemStack itemStack) {
        return enabled && SlimefunItem.getByItem(itemStack) != null;
    }

}
