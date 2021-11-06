package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface StackedItem extends AsyncStackedObject<Item> {

    /**
     * Get the item object of bukkit.
     */
    Item getItem();


    /**
     * Get the uuid of the item.
     */
    UUID getUniqueId();

    /**
     * Get the custom name of the item.
     */
    String getCustomName();

    /**
     * Set a custom-name to the item.
     *
     * @param customName a new custom name
     */
    void setCustomName(String customName);

    /**
     * Check whether or not the name of the item is visible.
     */
    boolean isCustomNameVisible();

    /**
     * Set visible custom name flag to the item.
     *
     * @param visible should the custom name of the item be visible or not
     */
    void setCustomNameVisible(boolean visible);

    /**
     * Get the item stack of the item.
     * A duplicated item stack with the stacked object's amount.
     */
    ItemStack getItemStack();

    /**
     * Set the item stack of the item.
     * If null or air, the remove method will be called.
     *
     * @param itemStack a new item stack
     */
    void setItemStack(ItemStack itemStack);

    /**
     * Add the duplicated item stack to an inventory.
     * Respects settings, such as 'item fix stack' and more
     *
     * @param inventory inventory to add the item to
     */
    void giveItemStack(Inventory inventory);

}
