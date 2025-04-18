package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface NMSAdapter {

    default void loadLegacy() {

    }

    INMSEntityEquipment createEntityEquipmentWrapper(EntityEquipment bukkitEntityEquipment);

    boolean shouldArmorBeDamaged(ItemStack itemStack);

    boolean isUnbreakable(ItemStack itemStack);

    void makeItemGlow(ItemMeta itemMeta);

    ItemStack getPlayerSkull(ItemStack bukkitItem, String texture);

    void updateEntity(LivingEntity source, LivingEntity target);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    default void setOminousBottleAmplifier(ItemMeta itemMeta, int amplifier) {
        // Not implemented
    }

    Object getChatMessage(String message);

    void runAtEndOfTick(Runnable code);

    void saveEntity(StackedEntity stackedEntity);

    void loadEntity(StackedEntity stackedEntity);

    void saveItem(StackedItem stackedItem);

    void loadItem(StackedItem stackedItem);

}
