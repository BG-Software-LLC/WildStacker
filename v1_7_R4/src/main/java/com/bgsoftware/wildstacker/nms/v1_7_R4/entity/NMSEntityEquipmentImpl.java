package com.bgsoftware.wildstacker.nms.v1_7_R4.entity;

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
        return this.bukkitEntityEquipment.getItemInHandDropChance();
    }

    @Override
    public float getItemInOffHandDropChance() {
        return this.getItemInMainHandDropChance();
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        this.bukkitEntityEquipment.setItemInHand(itemStack);
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        this.setItemInMainHand(itemStack);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return this.bukkitEntityEquipment.getItemInHand();
    }

    @Override
    public ItemStack getItemInOffHand() {
        return this.getItemInMainHand();
    }

}
