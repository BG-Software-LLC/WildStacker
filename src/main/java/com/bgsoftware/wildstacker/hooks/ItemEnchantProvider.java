package com.bgsoftware.wildstacker.hooks;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface ItemEnchantProvider {

    boolean hasEnchantmentLevel(ItemStack itemStack, Enchantment enchantment, int requiredLevel);

}
