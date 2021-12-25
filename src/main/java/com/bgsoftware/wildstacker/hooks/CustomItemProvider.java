package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

public interface CustomItemProvider {

    boolean canCreateBarrel(ItemStack itemStack);

}
