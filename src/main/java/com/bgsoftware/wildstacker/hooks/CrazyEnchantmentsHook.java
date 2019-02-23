package com.bgsoftware.wildstacker.hooks;

import me.badbones69.crazyenchantments.Main;
import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CEnchantments;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public final class CrazyEnchantmentsHook {

    private static boolean enabled = false;

    public static int getNewExpValue(int expValue, ItemStack itemStack){
        if(enabled && itemStack != null && Main.CE.hasEnchantment(itemStack, CEnchantments.INQUISITIVE) && Methods.randomPicker(3)){
            return expValue * (Main.CE.getPower(itemStack, CEnchantments.INQUISITIVE) + 1);
        }
        return expValue;
    }

    public static void register(){
        enabled = Bukkit.getPluginManager().isPluginEnabled("CrazyEnchantments");
    }

}
