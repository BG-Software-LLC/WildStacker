package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildtools.api.WildToolsAPI;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.inventory.ItemStack;

public final class WildToolsHook {

    public static boolean hasSilkTouch(ItemStack itemStack){
        if(PluginHooks.isWildToolsEnabled){
            Tool tool = WildToolsAPI.getTool(itemStack);
            return tool != null && tool.hasSilkTouch();
        }

        return false;
    }

}
