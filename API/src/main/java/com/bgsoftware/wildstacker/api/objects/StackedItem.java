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
     * Set the item stack of the item.
     * If null or air, the remove method will be called.
     * @param itemStack a new item stack
     */
    void setItemStack(ItemStack itemStack);

    /**
     * Get the item stack of the item.
     * A duplicated item stack with the stacked object's amount.
     */
    ItemStack getItemStack();

    /**
     * Add the duplicated item stack to an inventory.
     * Respects settings, such as 'item fix stack' and more
     * @param inventory inventory to add the item to
     */
    void giveItemStack(Inventory inventory);

}
