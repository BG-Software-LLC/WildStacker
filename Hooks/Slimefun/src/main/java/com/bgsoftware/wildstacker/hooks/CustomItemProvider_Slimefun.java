package com.bgsoftware.wildstacker.hooks;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.inventory.ItemStack;

public final class CustomItemProvider_Slimefun implements CustomItemProvider {

    @Override
    public boolean canCreateBarrel(ItemStack itemStack) {
        return SlimefunItem.getByItem(itemStack) == null;
    }

}
