package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public interface StackedBarrel extends StackedObject<Block> {

    /**
     * Get the block object of bukkit.
     */
    Block getBlock();

    /**
     * Get the material type of the block that is inside the barrel.
     */
    Material getType();

    /**
     * Get the data value of the block that is inside the barrel..
     */
    short getData();

    /**
     * Display the block armor-stand inside the cauldron.
     * Will not be ran if already displayed.
     */
    void createDisplayBlock();

    /**
     * Destroy the block armor-stand inside the cauldron.
     */
    void removeDisplayBlock();

    /**
     * Get the block armor-stand inside the cauldron.
     */
    ArmorStand getDisplayBlock();

    /**
     * Get the block inside the item as an item-stack.
     * @param amount the amount of the item-stack.
     */
    ItemStack getBarrelItem(int amount);

}
