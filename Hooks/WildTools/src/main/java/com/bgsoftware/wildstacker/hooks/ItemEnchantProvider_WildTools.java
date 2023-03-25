package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildtools.api.WildToolsAPI;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class ItemEnchantProvider_WildTools implements ItemEnchantProvider {

    @Override
    public boolean hasEnchantmentLevel(ItemStack itemStack, Enchantment enchantment, int requiredLevel) {
        if(enchantment != Enchantment.SILK_TOUCH)
            return false;

        Tool tool = WildToolsAPI.getTool(itemStack);
        return tool != null && tool.hasSilkTouch();
    }

}
