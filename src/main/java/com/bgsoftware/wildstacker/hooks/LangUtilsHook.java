package com.bgsoftware.wildstacker.hooks;

import com.meowj.langutils.lang.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public final class LangUtilsHook {

    public static boolean enabled;

    public LangUtilsHook() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("LangUtils");
    }

    public static String getItemName(ItemStack item) {
        return LanguageHelper.getItemName(item, "tr_TR");
    }

}
