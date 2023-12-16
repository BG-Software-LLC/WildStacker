package com.bgsoftware.wildstacker.nms.v1_20_3.entity;

import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public final class NMSEntityEquipmentImpl implements INMSEntityEquipment {

    private final EntityEquipment bukkitEntityEquipment;

    public NMSEntityEquipmentImpl(EntityEquipment bukkitEntityEquipment) {
        this.bukkitEntityEquipment = bukkitEntityEquipment;
    }

    @Override
    public float getItemInMainHandDropChance() {
        return this.bukkitEntityEquipment.getItemInMainHandDropChance();
    }

    @Override
    public float getItemInOffHandDropChance() {
        return this.bukkitEntityEquipment.getItemInOffHandDropChance();
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        this.bukkitEntityEquipment.setItemInMainHand(itemStack);
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        this.bukkitEntityEquipment.setItemInOffHand(itemStack);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return this.bukkitEntityEquipment.getItemInMainHand();
    }

    @Override
    public ItemStack getItemInOffHand() {
        return this.bukkitEntityEquipment.getItemInOffHand();
    }

}
