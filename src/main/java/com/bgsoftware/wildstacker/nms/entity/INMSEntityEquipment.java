package com.bgsoftware.wildstacker.nms.entity;

import org.bukkit.inventory.ItemStack;

public interface INMSEntityEquipment {

    float getItemInMainHandDropChance();

    float getItemInOffHandDropChance();

    void setItemInMainHand(ItemStack itemStack);

    void setItemInOffHand(ItemStack itemStack);

    ItemStack getItemInMainHand();

    ItemStack getItemInOffHand();

}
